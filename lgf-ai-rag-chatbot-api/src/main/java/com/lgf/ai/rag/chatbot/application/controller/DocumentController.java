package com.lgf.ai.rag.chatbot.application.controller;

import com.lgf.ai.rag.chatbot.application.service.IDocumentService;
import com.lgf.ai.rag.chatbot.domain.entity.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
@Tag(name = "Document API", description = "Operations related to document management.")
public class DocumentController {

    private final IDocumentService documentService;

    public DocumentController(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "Get paginated and filtered master documents by name")
    public ResponseEntity<Page<Document>> getPagedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        Page<Document> documents = documentService.getDocumentsBy(page, size, name);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and all its related data")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        boolean deleted = documentService.deleteDocumentBy(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
