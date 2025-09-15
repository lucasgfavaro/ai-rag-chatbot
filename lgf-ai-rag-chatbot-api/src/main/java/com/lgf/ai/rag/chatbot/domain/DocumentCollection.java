package com.lgf.ai.rag.chatbot.domain;


import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import com.lgf.ai.rag.chatbot.infrastructure.persistence.DocumentChunkRepository;
import com.lgf.ai.rag.chatbot.infrastructure.persistence.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCollection implements DocumentCollectionRepository {

    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;

    @Override
    public Optional<Document> get(String id) {
        return documentRepository.findById(id)
                .map(document -> {
                    List<DocumentChunk> chunks = documentChunkRepository.findByDocumentId(id);
                    document.addChunks(chunks);
                    return document;
                })
                .or(() -> {
                    log.warn("Document with id {} not found", id);
                    return Optional.empty();
                });
    }

    @Override
    public Document addDocument(Document document) {

        Document saved = documentRepository.save(document);
        String documentId = saved.getId();

        for (DocumentChunk chunk : document.getChunks()) {
            DocumentChunk chunkWithDocId = new DocumentChunk(
                    chunk.id(), chunk.documentName(), chunk.text(),
                    chunk.lineNumber(), chunk.metadata(), documentId
            );
            documentChunkRepository.save(chunkWithDocId);
        }
        return saved;
    }

    @Override
    public Page<Document> getPagedDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Override
    public boolean deleteDocumentById(String documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return false;
        }
        documentChunkRepository.deleteByDocumentId(documentId);
        documentRepository.deleteById(documentId);
        return true;
    }

    public List<Document> getRelevantDocuments(List<String> chunkIds) {
        Map<String, Document> documentMap = new HashMap<>();
        for (String chunkId : chunkIds) {
            documentChunkRepository.findById(chunkId).ifPresent(chunk -> {
                documentRepository.findById(chunk.documentId()).ifPresent(doc -> {
                    Document document =
                            documentMap.computeIfAbsent(doc.getId(), k ->
                                    new Document(doc.getId(), doc.getName(), doc.getSize(), doc.getIngestedAt(), doc.getContent()));
                    document.getChunks().add(chunk);
                });
            });
        }
        return new ArrayList<>(documentMap.values());
    }

}
