package com.lgf.ai.rag.chatbot.domain;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DocumentCollectionRepository {

    Optional<Document> get(String id);

    Document addDocument(Document document);

    Page<Document> getPagedDocuments(Pageable pageable);

    boolean deleteDocumentById(String documentId);
}
