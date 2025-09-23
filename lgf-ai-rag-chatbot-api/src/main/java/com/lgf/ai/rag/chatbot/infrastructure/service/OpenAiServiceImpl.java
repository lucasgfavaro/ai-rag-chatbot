package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.lgf.ai.rag.chatbot.application.service.IEmbeddingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class OpenAiServiceImpl implements IEmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;
    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private final Encoding encoding = registry.getEncodingForModel(ModelType.TEXT_EMBEDDING_ADA_002);

    public OpenAiServiceImpl(OpenAiEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        registry.getEncodingForModel(ModelType.TEXT_EMBEDDING_ADA_002);
    }

    @Override
    public float[] embed(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("Empty or null content provided for embedding generation");
            return new float[0];
        }

        try {
            int tokenCount = encoding.countTokens(content);
            log.info("Generating embedding for content (length: {} characters, tokens: {})", content.length(), tokenCount);
            return embeddingModel.embed(content);
        } catch (Exception e) {
            log.error("Failed to generate embedding for content (length: {}): {}",
                    content.length(), e.getMessage(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }
}
