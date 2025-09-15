package com.lgf.ai.rag.chatbot.infrastructure.persistence;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<Document, String> {

}

