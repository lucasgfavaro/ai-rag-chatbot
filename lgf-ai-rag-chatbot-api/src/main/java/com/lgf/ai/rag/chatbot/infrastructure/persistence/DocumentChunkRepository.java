package com.lgf.ai.rag.chatbot.infrastructure.persistence;

import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentChunkRepository extends MongoRepository<DocumentChunk, String> {
    List<DocumentChunk> findByDocumentId(String documentId);

    void deleteByDocumentId(String documentId);
}