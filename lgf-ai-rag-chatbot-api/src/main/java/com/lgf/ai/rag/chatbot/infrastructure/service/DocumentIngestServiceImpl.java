package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.lgf.ai.rag.chatbot.application.service.IDocumentIngestService;
import com.lgf.ai.rag.chatbot.application.service.IDocumentService;
import com.lgf.ai.rag.chatbot.application.service.IVectorDatabaseService;
import com.lgf.ai.rag.chatbot.domain.chunking.DocumentChunker;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Log4j2
@Service
public class DocumentIngestServiceImpl implements IDocumentIngestService {

    private final IVectorDatabaseService vectorDatabaseService;
    private final IDocumentService documentService;
    private final DocumentChunker documentChunker;

    public DocumentIngestServiceImpl(IVectorDatabaseService vectorDatabaseService,
                                     DocumentChunker documentChunker,
                                     IDocumentService documentService) {
        this.vectorDatabaseService = vectorDatabaseService;
        this.documentChunker = documentChunker;
        this.documentService = documentService;
    }

    @Override
    public Document ingestDocument(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();
        log.info("Starting document ingestion process for file: {} (size: {} bytes)", name, fileSize);

        validateFile(file);
        String content = extractContent(file);
        Document document = createDocument(name, contentType, fileSize);
        addChunks(document, content);
        storeChunks(document);
        persistDocument(document);
        log.info("Document {} ingestion completed successfully: {} chunks processed", name, document.getChunks().size());
        return document;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }

    private String extractContent(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private Document createDocument(String name, String contentType, long fileSize) {
        return new Document(name, contentType, fileSize, LocalDateTime.now());
    }

    private void addChunks(Document document, String content) {
        document.addChunks(documentChunker.chunk(document, content));
    }

    private void storeChunks(Document document) {
        document.getChunks().forEach(vectorDatabaseService::store);
    }

    private void persistDocument(Document document) {
        documentService.createDocument(document);
    }
}
