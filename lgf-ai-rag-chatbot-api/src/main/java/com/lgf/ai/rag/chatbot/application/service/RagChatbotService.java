package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.DocumentChunkCollection;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class RagChatbotService {

    private final ChatClient chatClient;
    private final VectorDatabaseService pineconeService;
    private final DocumentChunkCollection documentChunkCollection;

    public RagChatbotService(ChatClient.Builder builder, VectorDatabaseService pineconeService, DocumentChunkCollection chatChunkService//            , ChatMemory chatMemory
    ) {
        this.chatClient = builder.build();
        this.pineconeService = pineconeService;
        this.documentChunkCollection = chatChunkService;
    }

    public String ask(String question) {
        var relevantDocumentChunks = getRelevantDocumentChunks(question);
        String context = buildContext(relevantDocumentChunks);
        String references = buildReferences(relevantDocumentChunks);
        String prompt = buildPrompt(context, question);
        String response = chatClient.prompt(prompt).call().content();

        return appendFooter(response, references);
    }

    private java.util.List<DocumentChunk> getRelevantDocumentChunks(String question) {
        var similarVectors = pineconeService.getSimilarText(question);
        return similarVectors.stream()
                .map(v -> documentChunkCollection.get(UUID.fromString(v.getId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private String buildContext(java.util.List<DocumentChunk> chunks) {
        StringBuilder contextBuilder = new StringBuilder();
        int refNum = 1;
        for (DocumentChunk chunk : chunks) {
            contextBuilder.append("[").append(refNum).append("] ")
                .append(chunk.text()).append("\n");
            refNum++;
        }
        return contextBuilder.toString();
    }

    private String buildReferences(java.util.List<DocumentChunk> chunks) {
        StringBuilder referencesBuilder = new StringBuilder();
        int refNum = 1;
        for (DocumentChunk chunk : chunks) {
            referencesBuilder.append("[").append(refNum).append("] ")
                .append(chunk.documentName()).append(" - Línea: ")
                .append(chunk.lineNumber()).append("\n");
            refNum++;
        }
        return referencesBuilder.toString();
    }

    private String buildPrompt(String context, String question) {
        String GENERAL_CONTEXT = "Contexto: \nFragmentos extraídos de documentos relevantes:\n%s";
        String RESPONSE_FORMAT = "Formato de respuesta: \nResponde solo con la información presente en el contexto. " +
                "Si no hay suficiente información, indícalo explícitamente. No inventes ni agregues datos externos.";
        return String.format(GENERAL_CONTEXT, context) + "\n\n" + RESPONSE_FORMAT + "\n\n" + "Pregunta: " + question;
    }

    private String appendFooter(String response, String references) {
        String FOOTER = "\n\nReferencias:\n%s";
        return response + String.format(FOOTER, references);
    }


}
