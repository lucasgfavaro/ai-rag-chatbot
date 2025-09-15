package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import org.springframework.data.domain.Page;

public interface IDocumentService {

    void createDocument(Document document);

    Page<Document> getDocumentsBy(int page, int size, String name);

    Document getDocumentBy(String id);

    boolean deleteDocumentBy(String documentId);


}
