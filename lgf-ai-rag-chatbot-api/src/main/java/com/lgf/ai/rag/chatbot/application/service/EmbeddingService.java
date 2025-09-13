package com.lgf.ai.rag.chatbot.application.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;
    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private final Encoding encoding = registry.getEncodingForModel(ModelType.TEXT_EMBEDDING_ADA_002);

    public EmbeddingService(OpenAiEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        registry.getEncodingForModel(ModelType.TEXT_EMBEDDING_ADA_002);
    }


    public float[] embed(String content) {
        return embeddingModel.embed(content);
    }
}
