package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.List;

public interface IVectorDatabaseService {

    void store(DocumentChunk documentChunk);

    List<ScoredVectorWithUnsignedIndices> getSimilarText(String query);

    void deleteVectorsByChunkIds(List<String> chunkIds);
}
