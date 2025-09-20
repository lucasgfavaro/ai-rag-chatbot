package com.lgf.ai.rag.chatbot.domain.chunking;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class DocumentChunker {

    private final List<ChunkerStrategy>chunkerStrategies;

    public DocumentChunker(List<ChunkerStrategy> chunkerStrategies) {
        this.chunkerStrategies = chunkerStrategies;
        log.info("DocumentChunker initialized successfully");
    }

    public List<DocumentChunk>  chunk(Document document, String text) {
        String documentName = document.getName();
        String documentId = document.getId();
        log.info("Starting document chunking process for: {} (content length: {} characters)", documentName, text.length());

        if (text.trim().isEmpty()) {
            log.warn("Empty or null text provided for chunking document: {}", documentName);
            return List.of();
        }

        try {
            long startTime = System.currentTimeMillis();

            Optional<ChunkerStrategy> chunkerStrategy = chunkerStrategies.stream()
                    .filter(strategy -> strategy.isChunkingEnabled(text))
                    .findFirst();

            if (chunkerStrategy.isEmpty())
                throw new IllegalStateException("No chunking strategy available");

            List<DocumentChunk> chunks = chunkerStrategy.get().chunk(text, document);

            for (DocumentChunk chunk : chunks) {
                String id = UUID.randomUUID().toString();
                chunk.metadata().put("id", id);
                if (chunk.documentId() == null || chunk.documentId().isEmpty()) {
                    chunk.metadata().put("documentId", documentId);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("Chunking completed in {} ms, {} chunks generated", (endTime - startTime), chunks.size());
            return chunks;
        } catch (Exception e) {
            log.error("Error during chunking document: {}", documentName, e);
            return List.of();
        }
    }
}
