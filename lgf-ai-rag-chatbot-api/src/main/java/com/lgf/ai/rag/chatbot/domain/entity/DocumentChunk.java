package com.lgf.ai.rag.chatbot.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "document_chunks")
public record DocumentChunk(@Id @Field("_id") String id, String documentName, String text,
                            int lineNumber, Map<String, Object> metadata, String documentId) {
    @Override
    public String toString() {
        return String.format("Chunk: %s - Line: %d\n%s\nMetadata: %s\nDocumentId: %s", documentName, lineNumber, text, metadata, documentId);
    }

}