package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.google.protobuf.Struct;
import com.lgf.ai.rag.chatbot.application.service.IEmbeddingService;
import com.lgf.ai.rag.chatbot.application.service.IVectorDatabaseService;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;

@Log4j2
@Service
public class PineconeServiceImpl implements IVectorDatabaseService {

    private final IEmbeddingService embeddingService;
    private final Pinecone pinecone;
    private final String indexName;

    public PineconeServiceImpl(IEmbeddingService embeddingService, @Value("${pinecone.apiKey}") String apiKey,
                               @Value("${pinecone.index-name}") String indexName) {
        this.embeddingService = embeddingService;
        this.pinecone = new Pinecone.Builder(apiKey).build();
        this.indexName = indexName;
        log.info("VectorDatabaseServiceImpl initialized with index: {}", indexName);
    }

    @Override
    public void store(DocumentChunk documentChunk) {
        String vectorId = documentChunk.id();
        log.info("Starting vector storage process for document chunk: {}", vectorId);

        try {
            long startTime = System.currentTimeMillis();

            Index index = pinecone.getIndexConnection(indexName);
            log.debug("Connected to Pinecone index: {}", indexName);

            String content = documentChunk.text();
            log.debug("Generating embedding for chunk {} (content length: {} characters)", vectorId, content.length());

            float[] embedding = embeddingService.embed(content);
            log.debug("Embedding generated successfully for chunk {} (dimensions: {})", vectorId, embedding.length);

            List<Float> embeddingList = new ArrayList<>(embedding.length);
            for (float value : embedding) {
                embeddingList.add(value);
            }

            List<String> upsertIds = List.of(vectorId);
            List<List<Float>> values = List.of(embeddingList);

            Struct.Builder metadataStructBuilder = Struct.newBuilder();
            metadataStructBuilder.putFields("id", com.google.protobuf.Value.newBuilder().setStringValue(documentChunk.id()).build());
            List<Struct> metadataStructList = List.of(metadataStructBuilder.build());

            List<VectorWithUnsignedIndices> vectors = new ArrayList<>(1);
            for (int i = 0; i < metadataStructList.size(); i++) {
                vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), null, null, metadataStructList.get(i)));
            }

            UpsertResponse upsertResponse = index.upsert(vectors, "Default");

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("DocumentChunk with ID {} successfully stored in Pinecone in {} ms", vectorId, processingTime);
            log.debug("Pinecone upsert response: {}", upsertResponse);

        } catch (Exception e) {
            log.error("Failed to store DocumentChunk {} in Pinecone: {}", vectorId, e.getMessage(), e);
            throw new RuntimeException("Vector storage failed for chunk: " + vectorId, e);
        }
    }

    @Override
    public List<ScoredVectorWithUnsignedIndices> getSimilarText(String query) {
        log.info("Searching for similar vectors with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            log.warn("Empty or null query provided for vector search");
            return List.of();
        }

        try {
            long startTime = System.currentTimeMillis();

            Index index = pinecone.getIndexConnection(indexName);
            log.debug("Connected to Pinecone index: {} for similarity search", indexName);

            float[] embedding = embeddingService.embed(query);
            log.debug("Query embedding generated (dimensions: {})", embedding.length);

            List<Float> embeddingList = new ArrayList<>(embedding.length);
            for (float value : embedding) {
                embeddingList.add(value);
            }

            List<ScoredVectorWithUnsignedIndices> result = index.queryByVector(4, embeddingList, "Default", true, true).getMatchesList();

            long searchTime = System.currentTimeMillis() - startTime;
            log.info("Pinecone similarity search completed: found {} results for query '{}' in {} ms",
                    result.size(), query, searchTime);

            if (log.isDebugEnabled()) {
                result.forEach(vector ->
                        log.debug("Found similar vector - ID: {}, Score: {}", vector.getId(), vector.getScore())
                );
            }

            return result;

        } catch (Exception e) {
            log.error("Error during similarity search for query '{}': {}", query, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void deleteVectorsByChunkIds(List<String> chunkIds) {
        log.info("Deleting vectors in Pinecone for chunk IDs: {}", chunkIds);
        if (chunkIds == null || chunkIds.isEmpty()) {
            log.warn("No chunk IDs provided for deletion");
            return;
        }
        try {
            Index index = pinecone.getIndexConnection(indexName);
            log.debug("Connected to Pinecone index: {} for deletion", indexName);
            index.deleteByIds(chunkIds,"Default");
            log.info("Successfully deleted {} vectors from Pinecone", chunkIds.size());
        } catch (Exception e) {
            log.error("Error deleting vectors from Pinecone: {}", e.getMessage(), e);
            throw new RuntimeException("Vector deletion failed for chunk IDs: " + chunkIds, e);
        }
    }
}
