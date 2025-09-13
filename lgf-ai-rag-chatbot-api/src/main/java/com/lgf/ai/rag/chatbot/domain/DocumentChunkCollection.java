package com.lgf.ai.rag.chatbot.domain;


import com.lgf.ai.rag.chatbot.domain.entity.DocumentChunk;
import com.lgf.ai.rag.chatbot.infrastructure.persistence.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkCollection implements DocumentChunkCollectionRepository {

    private final DocumentChunkRepository documentChunkRepository;

    @Override
    public DocumentChunk add(DocumentChunk documentChunk) {
        documentChunkRepository.save(documentChunk);
        return documentChunk;
    }

    @Override
    public Optional<DocumentChunk> get(UUID id) {
        return documentChunkRepository.findById(id);
    }
}
