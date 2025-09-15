package com.lgf.ai.rag.chatbot.domain.chunking;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownChunker implements ChunkerStrategy {

    @Override
    public boolean isChunkingEnabled(String text) {
        return text.contains("# ") || text.contains("## ") || text.contains("### ");
    }

    @Override
    public List<DocumentChunk> chunk(String text, Document document) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        assert document != null;

        List<DocumentChunk> chunks = new ArrayList<>();
        String[] lines = text.split("\n");
        List<String> currentTitles = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int chunkStartLine = 0;
        boolean hasContent = false;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];

            // Ignorar líneas vacías y líneas de separación
            if (line.trim().isEmpty() || line.trim().matches("^-+$")) {
                continue;
            }

            // Regex robusto para títulos markdown
            Matcher matcher = Pattern.compile("^\\s*(#+)\\s*(.*)$").matcher(line.trim());

            if (matcher.matches()) {
                // Es un título
                int level = matcher.group(1).length();
                String title = matcher.group(2).trim();

                // Si es un artículo (###) Y ya tengo contenido desarrollado, crear chunk del artículo anterior
                if (level == 3 && hasContent && !currentChunk.isEmpty()) {

                    createChunk(chunks, currentChunk.toString().trim(), currentTitles, chunkStartLine, document.getName(), document.getId());
                    currentChunk.setLength(0);
                    hasContent = false;
                }

                // Actualizar jerarquía de títulos
                updateTitleHierarchy(currentTitles, level, title);

                // Si es un artículo, empezar nuevo chunk
                if (level == 3) {
                    chunkStartLine = lineNumber;
                    currentChunk.setLength(0);
                    hasContent = false;
                }

                currentChunk.append(line).append("\n");

            } else {
                // Es contenido
                currentChunk.append(line).append("\n");

                // Marcar que hay contenido real si no es línea vacía
                if (!line.trim().isEmpty()) {
                    hasContent = true;
                }
            }
        }

        // Crear último chunk si hay contenido
        if (hasContent && !currentChunk.isEmpty()) {
            createChunk(chunks, currentChunk.toString().trim(), currentTitles, chunkStartLine, document.getName(), document.getId());
        }

        return chunks;
    }

    private void updateTitleHierarchy(List<String> currentTitles, int level, String title) {
        if (level <= currentTitles.size()) {
            currentTitles.subList(level - 1, currentTitles.size()).clear();
        }

        while (currentTitles.size() < level - 1) {
            currentTitles.add("");
        }

        currentTitles.add(title);
    }

    private void createChunk(List<DocumentChunk> chunks, String text, List<String> titles, int lineNumber, String documentName, String documentId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("titles", new ArrayList<>(titles));
        chunks.add(new DocumentChunk(UUID.randomUUID().toString(), documentName, text, lineNumber, metadata, documentId));
    }


}
