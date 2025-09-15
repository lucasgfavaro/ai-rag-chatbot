package com.lgf.ai.rag.chatbot.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {
    @Id
    private String id;
    private String name;
    private String contentType;
    private long size;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ingestedAt;
    private String content;
    @Transient
    private List<DocumentChunk> chunks;

    public Document() {
    }

    public Document(String name, String contentType, long size, LocalDateTime ingestedAt, String content) {
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.ingestedAt = ingestedAt;
        this.content = content;
    }

    public void addChunks(List<DocumentChunk> chunks) {
        this.chunks = chunks;
    }

    public List<DocumentChunk> getChunks() {
        if (chunks == null) {
            chunks = new ArrayList<>();
        }
        return chunks;
    }
}

