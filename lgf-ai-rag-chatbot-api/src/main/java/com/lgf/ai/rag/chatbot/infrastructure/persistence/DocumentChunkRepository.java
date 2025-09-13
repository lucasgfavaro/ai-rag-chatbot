package com.lgf.ai.rag.chatbot.infrastructure.persistence;

import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface DocumentChunkRepository extends MongoRepository<DocumentChunk, UUID> {

}