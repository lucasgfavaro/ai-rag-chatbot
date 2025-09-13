package com.lgf.ai.rag.chatbot.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public record DocumentChunk(@Id @Field("_id") String id, String documentName, String text, int tokenCount, double kbSize,
                            int lineNumber) {
    @Override
    public String toString() {
        return String.format("Chunk: %s - %d Tks - %.2f KB - Line: %d\n%s", documentName, tokenCount, kbSize, lineNumber, text);
    }

}