package com.lgf.ai.rag.chatbot.application.service;

import com.google.protobuf.Struct;
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
public class VectorDatabaseService {

    private final EmbeddingService embeddingService;
    private final Pinecone pinecone;
    private final String indexName;

    public VectorDatabaseService(EmbeddingService embeddingService, @Value("${pinecone.apiKey}") String apiKey,
                                 @Value("${pinecone.index-name}") String indexName) {
        this.embeddingService = embeddingService;
        this.pinecone = new Pinecone.Builder(apiKey).build();
        this.indexName = indexName;
    }

    public void store(DocumentChunk documentChunk) {
        try {
            Index index = pinecone.getIndexConnection(indexName);
            String content = documentChunk.text();
            float[] embedding = embeddingService.embed(content);
            List<Float> embeddingList = new ArrayList<>(embedding.length);
            for (float value : embedding) {
                embeddingList.add(value); // autoboxing float → Float
            }
            String vectorId = documentChunk.id(); // Cambia a documentChunk.getId() si usas getter
            List<String> upsertIds = List.of(vectorId);
            List<List<Float>> values = List.of(embeddingList);
            Struct.Builder metadataStructBuilder = Struct.newBuilder();
            metadataStructBuilder.putFields("id", com.google.protobuf.Value.newBuilder().setStringValue(documentChunk.id()).build()); // Cambia a documentChunk.getId() si usas getter
            List<Struct> metadataStructList = List.of(metadataStructBuilder.build());
            List<VectorWithUnsignedIndices> vectors = new ArrayList<>(1);
            for (int i = 0; i < metadataStructList.size(); i++) {
                vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), null, null, metadataStructList.get(i)));
            }

            UpsertResponse upsertResponse = index.upsert(vectors, "Default");

            log.info("DocumentChunk con ID {} almacenado en Pinecone.", vectorId);
        } catch (Exception e) {
            log.error("Error al almacenar el DocumentChunk en Pinecone: {}", e.getMessage(), e);
        }
    }

// LF: The problem with this is that the raw data is stored in the database
//
//    public void store(DocumentChunk chatChunk) {
//
//        Document document = new Document(
//                chatChunk.getMessagesAsString(),
//                chatChunk.getMetadata().entrySet().stream()
//                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()))
//        );
//
//        log.debug("Document {} to be Embedded {} ", document.getId(), document.getFormattedContent());
//        vectorStore.add(List.of(document));
//    }

    public List<ScoredVectorWithUnsignedIndices> getSimilarText(String query) {

        Index index = pinecone.getIndexConnection(indexName);
        float[] embedding = embeddingService.embed(query);
        List<Float> embeddingList = new ArrayList<>(embedding.length);
        for (float value : embedding) {
            embeddingList.add(value); // autoboxing float → Float
        }

        List<ScoredVectorWithUnsignedIndices> result = index.queryByVector(4, embeddingList, "Default", true, true).getMatchesList();

        log.info("Querying Pinecone embedding '{}' results: {}", query, result.size());

        return result;
    }
}
