package com.lgf.ai.rag.chatbot.domain.chunking;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;

import java.util.List;

public interface ChunkerStrategy {

    boolean isChunkingEnabled(String text);

    List<DocumentChunk> chunk(String text, Document document);
}

