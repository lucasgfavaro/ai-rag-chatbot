package com.lgf.ai.rag.chatbot.domain;

import com.lgf.ai.rag.chatbot.application.service.EmbeddingService;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class DocumentChunker {

    private final EmbeddingService encodingService;

    public DocumentChunker(EmbeddingService encodingService) {
        this.encodingService = encodingService;
    }

    public List<DocumentChunk> chunk(String documentName, String text) {
        List<String> texts = parseDocument(text);
        List<DocumentChunk> chunks = new ArrayList<>();
        int lineNumber = 1;
        for (String chunkText : texts) {
            //int tokenCount = encodingService.getTotalTokensFrom(chunkText);
            double kbSize = chunkText.getBytes(StandardCharsets.UTF_8).length / 1024.0;
            String id = UUID.randomUUID().toString();
            chunks.add(new DocumentChunk(id, documentName, chunkText, 0, kbSize, lineNumber));
            lineNumber += chunkText.split("\r?\n").length;
        }
        log.info("Total Chunks: {} Total Size: {} KB", chunks.size(), chunks.stream().mapToDouble(DocumentChunk::kbSize).sum());
        return chunks;
    }

    private List<String> parseDocument(String text) {
        List<String> chunks = new ArrayList<>();
        String[] lines = text.split("\r?\n");
        StringBuilder currentChunk = new StringBuilder();
        String currentHeader = "";
        for (String line : lines) {
            // Detectar encabezados de capítulo o artículo
            if (line.matches("^Capítulo\\s+[IVXLCDM]+.*") || line.matches("^Artículo\\s+\\d+\\..*")) {
                // Si hay contenido previo, guardar como chunk
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentHeader + "\n" + currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                currentHeader = line;
            } else {
                currentChunk.append(line).append("\n");
            }
        }
        // Agregar el último chunk si existe
        if (!currentChunk.isEmpty()) {
            chunks.add(currentHeader + "\n" + currentChunk.toString().trim());
        }
        log.info("Total Chunks Parsed: {}", chunks.size());
        return chunks;
    }


}
