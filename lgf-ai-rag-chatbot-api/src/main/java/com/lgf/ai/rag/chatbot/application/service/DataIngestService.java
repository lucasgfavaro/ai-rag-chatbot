package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.DocumentChunkCollection;
import com.lgf.ai.rag.chatbot.domain.DocumentChunker;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Log4j2
@Service
public class DataIngestService {

    private final VectorDatabaseService vectorDatabaseService;
    private final DocumentChunker documentChunker;
    private final EmbeddingService embeddingService;
    private final DocumentChunkCollection documentDatabaseService;

    public DataIngestService(VectorDatabaseService pineconeService
            , DocumentChunker documentChunker
            , EmbeddingService embeddingService
            , DocumentChunkCollection documentChunkService) {
        this.vectorDatabaseService = pineconeService;
        this.documentChunker = documentChunker;
        this.embeddingService = embeddingService;
        this.documentDatabaseService = documentChunkService;
    }

    public void ingestDocument(MultipartFile file) throws IOException {
        String documentText = new String(file.getBytes(), StandardCharsets.UTF_8);
        String documentName = file.getOriginalFilename();
        List<DocumentChunk> documentChunks = documentChunker.chunk(documentName, documentText);
        documentChunks.forEach(chunk -> {
            DocumentChunk documentChunk = documentDatabaseService.add(chunk);
            vectorDatabaseService.store(documentChunk);
        });
    }
}
