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
        log.info("DataIngestServiceImpl initialized successfully");
    }

    @Override
    public Document ingestDocument(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();
        log.info("Starting document ingestion process for file: {} (size: {} bytes)", name, fileSize);

        if (file.isEmpty()) {
            log.warn("Attempted to ingest empty file: {}", name);
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            long startTime = System.currentTimeMillis();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            log.debug("File content extracted successfully for: {} (content length: {} characters)", name, content.length());

            Document document = new Document(name, contentType, fileSize, LocalDateTime.now(), content);

            document.addChunks(documentChunker.chunk(content, document));
            log.info("Document {} chunked into {} pieces", name, document.getChunks().size());

            document.getChunks().forEach(vectorDatabaseService::store);
            log.info("All chunks for document {} stored in vector database", name);

            documentService.createDocument(document);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Document {} ingestion completed successfully: {} chunks processed in {} ms",
                    name, document.getChunks().size(), processingTime);

            return document;
        } catch (IOException e) {
            log.error("IO error while processing document {}: {}", name, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during document ingestion for {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Document ingestion failed", e);
        }
    }
}
