package com.lgf.ai.rag.chatbot.application.service;

import com.lgf.ai.rag.chatbot.domain.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IDocumentIngestService {

    Document ingestDocument(MultipartFile file) throws IOException;

}
