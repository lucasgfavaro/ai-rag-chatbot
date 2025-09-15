package com.lgf.ai.rag.chatbot.application.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class QuestionAnswer {
    private String question;
    private String answer;

}

