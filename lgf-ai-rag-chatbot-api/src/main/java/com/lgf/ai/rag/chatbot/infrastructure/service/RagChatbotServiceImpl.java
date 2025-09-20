package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.lgf.ai.rag.chatbot.application.service.IRagChatbotService;
import com.lgf.ai.rag.chatbot.application.service.IVectorDatabaseService;
import com.lgf.ai.rag.chatbot.domain.DocumentCollection;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class RagChatbotServiceImpl implements IRagChatbotService {

    private final ChatClient chatClient;
    private final IVectorDatabaseService vectorDatabaseService;
    private final DocumentCollection documentCollection;

    public RagChatbotServiceImpl(ChatClient.Builder builder,
                                 IVectorDatabaseService vectorDatabaseService,
                                 DocumentCollection documentCollection) {
        this.chatClient = builder.build();
        this.vectorDatabaseService = vectorDatabaseService;
        this.documentCollection = documentCollection;
        log.info("RagChatbotServiceImpl initialized successfully");
    }

    @Override
    public String ask(String question) {
        log.info("Processing question: {}", question);

        if (question == null || question.trim().isEmpty()) {
            log.warn("Empty or null question received");
            return "Please provide a valid question.";
        }

        try {
            long startTime = System.currentTimeMillis();

            var relevantDocumentChunks = getRelevantDocumentChunks(question);
            log.info("Found {} relevant document chunks for question", relevantDocumentChunks.size());

            if (relevantDocumentChunks.isEmpty()) {
                log.warn("No relevant document chunks found for question: {}", question);
                return "I couldn't find relevant information to answer your question. Please make sure documents have been ingested.";
            }

            String context = buildContext(relevantDocumentChunks);
            String references = buildReferences(relevantDocumentChunks);
            String prompt = buildPrompt(context, question);

            log.debug("Generated prompt for AI model (length: {} characters)", prompt.length());

            String response = chatClient.prompt(prompt).call().content();
            log.debug("AI model response received (length: {} characters)", response.length());

            String finalResponse = appendFooter(response, references);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Question processed successfully in {} ms", processingTime);

            return finalResponse;

        } catch (Exception e) {
            log.error("Error processing question '{}': {}", question, e.getMessage(), e);
            return "I apologize, but I encountered an error while processing your question. Please try again.";
        }
    }

    private java.util.List<Document> getRelevantDocumentChunks(String question) {
        log.debug("Searching for relevant document chunks for question: {}", question);

        try {
            var similarVectors = vectorDatabaseService.getSimilarText(question);
            log.debug("Vector database returned {} similar vectors", similarVectors.size());
            var documents = documentCollection.getRelevantDocuments(similarVectors.stream().map(ScoredVectorWithUnsignedIndices::getId).toList());
            log.debug("Successfully retrieved {} document chunks", similarVectors.size());
            return documents;

        } catch (Exception e) {
            log.error("Error retrieving relevant document chunks: {}", e.getMessage(), e);
            return java.util.List.of();
        }
    }

    private String buildContext(java.util.List<Document> documents) {
        log.debug("Building context from {} documents", documents.size());

        StringBuilder contextBuilder = new StringBuilder();
        int refNum = 1;
        for (Document doc : documents) {
            if (doc.getChunks() != null) {
                for (DocumentChunk chunk : doc.getChunks()) {
                    contextBuilder.append("[").append(refNum).append("] ")
                            .append(chunk.text()).append("\n");
                    refNum++;
                }
            }
        }

        String context = contextBuilder.toString();
        log.debug("Context built successfully (length: {} characters)", context.length());
        return context;
    }

    private String buildReferences(java.util.List<Document> documents) {
        log.debug("Building references from {} documents", documents.size());

        StringBuilder referencesBuilder = new StringBuilder();
        int refNum = 1;
        for (Document doc : documents) {
            if (doc.getChunks() != null) {
                for (DocumentChunk chunk : doc.getChunks()) {
                    referencesBuilder.append("[").append(refNum).append("] ");
                    referencesBuilder.append(chunk.documentName());
                    referencesBuilder.append(" - Línea ").append(chunk.order());
                    if (chunk.metadata() != null && !chunk.metadata().isEmpty()) {
                        String title = extractTitleFromMetadata(chunk.metadata());
                        if (title != null && !title.trim().isEmpty()) {
                            referencesBuilder.append(" - ").append(title);
                        }
                    }
                    referencesBuilder.append("\n");
                    refNum++;
                }
            }
        }
        return referencesBuilder.toString();
    }

    private String extractTitleFromMetadata(Map<String, Object> metadata) {
        // Primero buscar la clave 'titles' que contiene la jerarquía completa
        Object titlesObj = metadata.get("titles");
        if (titlesObj instanceof List<?> titlesList && !titlesList.isEmpty()) {
            // Construir el título jerárquico desde la lista de títulos
            StringBuilder titleBuilder = new StringBuilder();
            for (Object titleObj : titlesList) {
                if (titleObj != null && !titleObj.toString().trim().isEmpty()) {
                    if (titleBuilder.length() > 0) {
                        titleBuilder.append(" > ");
                    }
                    titleBuilder.append(titleObj.toString().trim());
                }
            }

            String hierarchicalTitle = titleBuilder.toString();
            if (!hierarchicalTitle.isEmpty()) {
                log.debug("Found hierarchical title in metadata: {}", hierarchicalTitle);
                return hierarchicalTitle;
            }
        }

        // Fallback: buscar diferentes posibles claves para títulos individuales
        String[] titleKeys = {"title", "titulo", "heading", "section", "header", "chapter", "capitulo"};

        for (String key : titleKeys) {
            Object value = metadata.get(key);
            if (value != null) {
                String titleValue = value.toString().trim();
                if (!titleValue.isEmpty()) {
                    log.debug("Found title in metadata with key '{}': {}", key, titleValue);
                    return titleValue;
                }
            }
        }

        log.debug("No title found in metadata keys: {}", metadata.keySet());
        return null;
    }

    private String buildPrompt(String context, String question) {
        log.debug("Building prompt for AI model");

        return String.format("""
                Based solely on the following context, answer the question precisely and concisely.
                
                Context:
                %s
                
                Question: %s
                
                Answer:""", context, question);
    }

    private String appendFooter(String response, String references) {
        return response + "\n\n--- Referencias ---\n\n" + references;
    }
}
