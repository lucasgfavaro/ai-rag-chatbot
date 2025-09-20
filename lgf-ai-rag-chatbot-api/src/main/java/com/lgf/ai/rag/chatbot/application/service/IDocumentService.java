package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDocumentService {

    void createDocument(Document document);

    Page<Document> getDocumentsBy(int page, int size, String name);

    Document getDocumentBy(String id);

    boolean deleteDocumentBy(String documentId);

    List<DocumentChunk> getChunksByDocumentId(String id);
}
