package com.lgf.ai.rag.chatbot.domain;

import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;

import java.util.Optional;
import java.util.UUID;

public interface DocumentChunkCollectionRepository {
    DocumentChunk add(DocumentChunk documentChunk);

    Optional<DocumentChunk> get(UUID id);
}

