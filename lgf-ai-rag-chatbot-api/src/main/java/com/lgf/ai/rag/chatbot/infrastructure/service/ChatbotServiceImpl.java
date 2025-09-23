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

		String systemMessage = "Eres un asistente de IA que ayuda a los usuarios respondiendo preguntas basadas en los documentos proporcionados. "
				+ "Si la respuesta no está contenida en los documentos, responde con 'No lo sé'. "
				+ "Mantén las respuestas concisas y directas.";

		if (question == null || question.trim().isEmpty()) {
			log.warn("Empty or null question received");
			return "Por favor ingresa una pregunta válida.";
		}

		try {
			long startTime = System.currentTimeMillis();

			var relevantDocuments = getRelevantDocument(question);
			log.info("Found {} relevant document chunks for question", relevantDocuments.size());

			String context = buildContext(relevantDocuments);
			String references = buildReferences(relevantDocuments);

			// TODO: QuestionAnswerAdvisor

			String prompt = buildPrompt(systemMessage, context, references, question, null); // null para historial por ahora

			String response = chatClient.prompt(prompt).call().content();
			assert response != null;
			String finalResponse = appendFooter(response, references);

			log.info("Question processed successfully ");
			return finalResponse;

		} catch (Exception e) {
			log.error("Error processing question '{}': {}", question, e.getMessage(), e);
			return "Lamento las molestias, pero se produjo un error al procesar tu pregunta. Por favor, inténtalo de nuevo.";
		}
	}

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
		try {
			var similarVectors = vectorDatabaseService.getSimilarText(question);
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

        return contextBuilder.toString();
	}

	private String buildReferences(java.util.List<Document> documents) {

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
		Object titlesObj = metadata.get("titles");
		if (titlesObj instanceof List<?> titlesList && !titlesList.isEmpty()) {
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
