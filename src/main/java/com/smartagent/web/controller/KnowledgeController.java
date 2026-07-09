package com.smartagent.web.controller;

import com.smartagent.rag.DocumentService;
import com.smartagent.rag.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Knowledge base controller - manage RAG documents.
 */
@Controller
public class KnowledgeController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    private final DocumentService documentService;

    public KnowledgeController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/api/knowledge/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "File is empty"));
            }

            log.info("Uploading document: {}", file.getOriginalFilename());
            List<String> chunkIds = documentService.uploadDocument(file);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Document processed successfully",
                    "chunks", chunkIds.size(),
                    "filename", file.getOriginalFilename()));
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false, "message", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/api/knowledge/list")
    @ResponseBody
    public ResponseEntity<List<VectorStore.StoredDocument>> listDocuments() {
        return ResponseEntity.ok(documentService.listDocuments());
    }

    @DeleteMapping("/api/knowledge/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearDocuments() {
        documentService.clearDocuments();
        return ResponseEntity.ok(Map.of("success", true, "message", "All documents cleared"));
    }
}
