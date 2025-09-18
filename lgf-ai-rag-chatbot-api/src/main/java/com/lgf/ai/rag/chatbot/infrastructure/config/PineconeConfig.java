package com.lgf.ai.rag.chatbot.infrastructure.config;


import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeConfig {

    @Bean
    public OpenAiEmbeddingModel embeddingModel(@Value("${spring.ai.openai.api-key}") String apikey) {
        // Can be any other EmbeddingModel implementation.
        return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(apikey).build());
    }
}
