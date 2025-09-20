package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.lgf.ai.rag.chatbot.application.service.IDocumentService;
import com.lgf.ai.rag.chatbot.application.service.IVectorDatabaseService;
import com.lgf.ai.rag.chatbot.domain.DocumentCollection;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class DocumentServiceImpl implements IDocumentService {

    private final IVectorDatabaseService vectorDatabaseService;
    private final DocumentCollection documentCollection;

    @Override
    public void createDocument(Document document) {
        documentCollection.addDocument(document);
    }

    @Override
    public Page<Document> getDocumentsBy(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        return documentCollection.getPagedDocuments(pageable);
    }

    @Override
    public Document getDocumentBy(String id) {
        return documentCollection.get(id).orElse(null);
    }

    @Transactional
    @Override
    public boolean deleteDocumentBy(String documentId) {
        var document = documentCollection.get(documentId);
        vectorDatabaseService.deleteVectorsByChunkIds(document.get().getChunks().stream().map(DocumentChunk::id).toList());
        documentCollection.deleteDocumentById(documentId);
        return true;
    }

    @Override
    public List<DocumentChunk> getChunksByDocumentId(String id) {
        Document document = documentCollection.get(id).orElse(null);
        if (document == null) {
            return List.of();
        }
        return document.getChunks();
    }
}
