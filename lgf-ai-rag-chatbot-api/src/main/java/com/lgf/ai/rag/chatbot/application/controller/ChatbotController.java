package com.lgf.ai.rag.chatbot.application.controller;

import com.lgf.ai.rag.chatbot.application.controller.model.QuestionAnswer;
import com.lgf.ai.rag.chatbot.application.service.IRagChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final IRagChatbotService ragChatbotService;

    public ChatbotController(IRagChatbotService ragChatbotService) {
        this.ragChatbotService = ragChatbotService;
    }

    /**
     * Endpoint to ask a question and get an answer based on the context of the ingested documents.
     *
     * @param question The question to be answered.
     * @return A QuestionAnswer object containing the question and the answer.
     */
    @PostMapping()
    @Operation(summary = "Ask a question and get an answer based on the context of the documents.",
            description = "Returns an answer using only the information present in the context of the ingested documents.")
    public ResponseEntity<QuestionAnswer> question(
            @Parameter(description = "The question to ask.") @RequestParam String question) {
        String answer = ragChatbotService.ask(question);
        QuestionAnswer qa = new QuestionAnswer(question, answer);
        return ResponseEntity.ok(qa);
    }
}
