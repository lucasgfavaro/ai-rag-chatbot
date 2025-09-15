package com.lgf.ai.rag.chatbot.application.controller;

import com.lgf.ai.rag.chatbot.application.service.IDocumentIngestService;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Log4j2
@RestController
@RequestMapping("/data-ingest")
@Tag(name = "Data Ingest API", description = "Endpoints for ingesting and deleting documents.")
public class DataIngestController {

    private final IDocumentIngestService dataIngestService;

    public DataIngestController(IDocumentIngestService dataIngestService) {
        this.dataIngestService = dataIngestService;
    }

    /**
     * Endpoint to ingest a document into the system.
     *
     * @param file The document file to ingest.
     * @return The processed and stored document.
     */
    @PostMapping("/document")
    @Operation(summary = "Ingest a document into the system.",
            description = "Uploads a document file and stores its content for later retrieval and processing.")
    public ResponseEntity<Document> ingestDocument(
            @Parameter(description = "The document file to ingest.") @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(dataIngestService.ingestDocument(file));
    }

}
