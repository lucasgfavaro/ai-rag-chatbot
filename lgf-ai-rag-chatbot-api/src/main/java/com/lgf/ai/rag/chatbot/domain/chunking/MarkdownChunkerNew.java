package com.lgf.ai.rag.chatbot.domain.chunking;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownChunkerNew implements ChunkerStrategy {

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
        // Usar split con -1 para conservar líneas vacías y espacios
        String[] lines = text.split("\n", -1);
        int totalLines = lines.length;

        // Ajuste automático del tamaño de chunk y solapamiento
        int minChunkSize = 10;
        int maxChunkSize = 50;
        int chunkSize = Math.max(minChunkSize, Math.min(maxChunkSize, totalLines / 10));
        int overlapSize = Math.max(2, chunkSize / 4);

        int start = 0;
        int order = 0;
        Pattern titlePattern = Pattern.compile("^(#+)\\s+(.*)");
        // Guardar títulos y su línea
        List<TitleInfo> titles = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = titlePattern.matcher(lines[i]);
            if (matcher.find()) {
                titles.add(new TitleInfo(i, lines[i].trim()));
            }
        }
        while (start < totalLines) {
            int end = Math.min(start + chunkSize, totalLines);
            StringBuilder chunkText = new StringBuilder();
            for (int i = start; i < end; i++) {
                chunkText.append(lines[i]);
                if (i < end - 1) chunkText.append("\n");
            }
            // Filtrar títulos relevantes para este chunk
            List<String> chunkTags = new ArrayList<>();
            for (TitleInfo title : titles) {
                if (title.lineNumber >= start && title.lineNumber < end) {
                    chunkTags.add(title.title);
                }
            }
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("startLine", start);
            metadata.put("endLine", end - 1);
            metadata.put("tags", chunkTags);
            metadata.put("documentId", document.getId());
            metadata.put("documentName", document.getName());
            chunks.add(new DocumentChunk(UUID.randomUUID().toString(), document.getName(), chunkText.toString(), order, metadata, document.getId()));
            order++;
            if (end == totalLines) break;
            start += (chunkSize - overlapSize);
        }
        return chunks;
    }

    // Helper para guardar info de títulos
    private static class TitleInfo {
        int lineNumber;
        String title;
        TitleInfo(int lineNumber, String title) {
            this.lineNumber = lineNumber;
            this.title = title;
        }
    }
}
