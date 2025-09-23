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
    }

    @Override
    public void store(DocumentChunk documentChunk) {
        String vectorId = documentChunk.id();
        log.info("Starting vector storage process for document chunk: {}", vectorId);

        try {
            Index index = pinecone.getIndexConnection(indexName);
            String content = documentChunk.text();
            float[] embedding = embeddingService.embed(content);

            List<Float> embeddingList = new ArrayList<>(embedding.length);
            for (float value : embedding) {
                embeddingList.add(value);
            }

            List<String> upsertIds = List.of(vectorId);
            List<List<Float>> values = List.of(embeddingList);

            Struct metadataStruct = buildMetadataStruct(documentChunk);
            List<Struct> metadataStructList = List.of(metadataStruct);

            List<VectorWithUnsignedIndices> vectors = new ArrayList<>(1);
            for (int i = 0; i < metadataStructList.size(); i++) {
                vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), null, null, metadataStructList.get(i)));
            }

            UpsertResponse upsertResponse = index.upsert(vectors, "Default");

            log.info("DocumentChunk with ID {} successfully stored in Pinecone", vectorId);
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
            Index index = pinecone.getIndexConnection(indexName);
            float[] embedding = embeddingService.embed(query);

            List<Float> embeddingList = new ArrayList<>(embedding.length);
            for (float value : embedding) {
                embeddingList.add(value);
            }

            List<ScoredVectorWithUnsignedIndices> result = index.queryByVector(4, embeddingList, "Default", true, true).getMatchesList();
            log.info("Pinecone similarity search completed: found {} results for query '{}'",
                    result.size(), query);

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
            index.deleteByIds(chunkIds, "Default");
            log.info("Successfully deleted {} vectors from Pinecone", chunkIds.size());
        } catch (Exception e) {
            log.error("Error deleting vectors from Pinecone: {}", e.getMessage(), e);
            throw new RuntimeException("Vector deletion failed for chunk IDs: " + chunkIds, e);
        }
    }

    private Struct buildMetadataStruct(DocumentChunk documentChunk) {
        Struct.Builder metadataStructBuilder = Struct.newBuilder();
        metadataStructBuilder.putFields("id", com.google.protobuf.Value.newBuilder().setStringValue(documentChunk.id()).build());
        if (documentChunk.metadata() != null) {
            documentChunk.metadata().forEach((key, value) -> {
                if (value != null) {
                    if (value instanceof String) {
                        metadataStructBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setStringValue((String) value).build());
                    } else if (value instanceof Number) {
                        metadataStructBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setNumberValue(((Number) value).doubleValue()).build());
                    } else if (value instanceof List<?> list) {
                        com.google.protobuf.ListValue.Builder listBuilder = com.google.protobuf.ListValue.newBuilder();
                        for (Object item : list) {
                            if (item != null) {
                                listBuilder.addValues(com.google.protobuf.Value.newBuilder().setStringValue(item.toString()).build());
                            }
                        }
                        metadataStructBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setListValue(listBuilder.build()).build());
                    } else {
                        metadataStructBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setStringValue(value.toString()).build());
                    }
                }
            });
        }
        return metadataStructBuilder.build();
    }
}
