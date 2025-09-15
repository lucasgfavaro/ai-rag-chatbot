package com.lgf.ai.rag.chatbot.application.service;

public interface IEmbeddingService {
    float[] embed(String content);
}
