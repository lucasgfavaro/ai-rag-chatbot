package com.lgf.ai.rag.chatbot.infrastructure.service;

import com.lgf.ai.rag.chatbot.application.service.IRagChatbotService;
import com.lgf.ai.rag.chatbot.application.service.IVectorDatabaseService;
import com.lgf.ai.rag.chatbot.domain.DocumentCollection;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class ChatbotServiceImpl implements IRagChatbotService {

	private final ChatClient chatClient;
	private final IVectorDatabaseService vectorDatabaseService;
	private final DocumentCollection documentCollection;

	@Value("${chatbot.similarity-threshold:0.75}")
	private double similarityThreshold;

	public ChatbotServiceImpl(ChatClient.Builder builder,
							  IVectorDatabaseService vectorDatabaseService,
							  DocumentCollection documentCollection) {
		this.chatClient = builder.build();
		this.vectorDatabaseService = vectorDatabaseService;
		this.documentCollection = documentCollection;
	}

	@Override
	public String ask(String question) {
		log.info("Processing question: {}", question);

		String systemMessage = "You are an AI assistant that helps users by answering questions based on provided documents. "
				+ "If the answer is not contained within the documents, respond with 'I don't know'. "
				+ "Keep answers concise and to the point.";

		if (question == null || question.trim().isEmpty()) {
			log.warn("Empty or null question received");
			return "Please provide a valid question.";
		}

		try {
			long startTime = System.currentTimeMillis();

			var relevantDocuments = getRelevantDocument(question);
			log.info("Found {} relevant document chunks for question", relevantDocuments.size());

			String context = buildContext(relevantDocuments);
			String references = buildReferences(relevantDocuments);


			//QuestionAnswerAdvisor

			String prompt = buildPrompt(systemMessage, context, references, question, null); // null para historial por ahora

			log.debug("Generated prompt for AI model (length: {} characters)", prompt.length());

			String response = chatClient.prompt(prompt).call().content();
			assert response != null;
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

	/**
	 * Construye el prompt para el modelo AI, ordenando system message, contexto, referencias y pregunta del usuario.
	 * Si se provee historial, lo incluye antes de la pregunta.
	 */
	private String buildPrompt(String systemMessage, String context, String references, String userQuestion, List<String> chatHistory) {
		StringBuilder sb = new StringBuilder();
		sb.append("System: ").append(systemMessage).append("\n\n");
		if (context != null && !context.isEmpty()) {
			sb.append("Context:\n").append(context).append("\n\n");
		}
		if (references != null && !references.isEmpty()) {
			sb.append("References:\n").append(references).append("\n\n");
		}
		if (chatHistory != null && !chatHistory.isEmpty()) {
			sb.append("Chat History:\n");
			for (String msg : chatHistory) {
				sb.append(msg).append("\n");
			}
			sb.append("\n");
		}
		sb.append("User: ").append(userQuestion);
		return sb.toString();
	}

	private java.util.List<Document> getRelevantDocument(String question) {
		log.debug("Searching for relevant document chunks for question: {}", question);

		try {
			var similarVectors = vectorDatabaseService.getSimilarText(question);
			log.debug("Vector database returned {} similar vectors", similarVectors.size());
			// Filtrar por score de similitud usando la propiedad
			var filteredIds = similarVectors.stream()
					.filter(vec -> vec.getScore() >= similarityThreshold)
					.map(ScoredVectorWithUnsignedIndices::getId)
					.toList();
			log.debug("Filtered to {} vectors above threshold {}", filteredIds.size(), similarityThreshold);
			var documents = documentCollection.getRelevantDocuments(filteredIds);
			log.debug("Successfully retrieved {} document chunks", documents.size());
			return documents;

		} catch (Exception e) {
			log.error("Error retrieving relevant document chunks: {}", e.getMessage(), e);
			return java.util.List.of();
		}
	}

	private String buildContext(java.util.List<Document> documents) {

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


	private String appendFooter(String response, String references) {
		return response + "\n\n--- Referencias ---\n\n" + references;
	}
}
