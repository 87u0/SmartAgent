package com.smartagent.rag;

import com.smartagent.rag.impl.LocalVectorStore;
import com.smartagent.rag.impl.ZhipuEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Document processing service.
 * Handles document parsing, chunking, and indexing.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    @Value("${smartagent.rag.chunk-size:500}")
    private int chunkSize;

    @Value("${smartagent.rag.chunk-overlap:50}")
    private int chunkOverlap;

    public DocumentService(EmbeddingService embeddingService, VectorStore vectorStore) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
    }

    /**
     * Upload and index a document.
     * Returns the list of chunk IDs created.
     */
    public List<String> uploadDocument(MultipartFile file) {
        try {
            String content;
            String filename = file.getOriginalFilename();

            // Parse based on file type
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                content = parsePdf(file);
            } else {
                // Plain text / markdown
                content = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }

            // Chunk the content
            List<String> chunks = chunkText(content);
            log.info("Document '{}' split into {} chunks", filename, chunks.size());

            // Create embeddings and store
            List<String> chunkIds = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = UUID.randomUUID().toString().substring(0, 8);
                String chunkText = chunks.get(i);

                // Add chunk metadata
                String indexedChunk = String.format("[Source: %s, Chunk: %d]\n%s",
                        filename != null ? filename : "unknown", i + 1, chunkText);

                List<Float> embedding = embeddingService.embed(chunkText);
                vectorStore.addDocument(chunkId, indexedChunk, embedding);
                chunkIds.add(chunkId);
            }

            return chunkIds;

        } catch (Exception e) {
            log.error("Failed to process document", e);
            throw new RuntimeException("Failed to process document: " + e.getMessage());
        }
    }

    /**
     * Search for relevant context using semantic search.
     */
    public String searchContext(String query, int topK) {
        if (topK <= 0) topK = 5;

        try {
            List<Float> queryEmbedding = embeddingService.embed(query);
            List<VectorStore.SearchResult> results = vectorStore.search(queryEmbedding, topK);

            if (results.isEmpty()) {
                return "";
            }

            StringBuilder context = new StringBuilder();
            context.append("Found ").append(results.size()).append(" relevant document chunks:\n\n");

            for (int i = 0; i < results.size(); i++) {
                VectorStore.SearchResult r = results.get(i);
                context.append("--- Chunk ").append(i + 1)
                        .append(" (similarity: ").append(String.format("%.2f", r.score()))
                        .append(") ---\n")
                        .append(r.content()).append("\n\n");
            }

            return context.toString();

        } catch (Exception e) {
            log.error("Search failed", e);
            return "Search failed: " + e.getMessage();
        }
    }

    public List<VectorStore.StoredDocument> listDocuments() {
        return vectorStore.getAllDocuments();
    }

    public void clearDocuments() {
        vectorStore.clear();
    }

    private String parsePdf(MultipartFile file) {
        try (var pdf = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            var stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(pdf);
        } catch (Exception e) {
            log.warn("PDF parsing failed: {}", e.getMessage());
            return "PDF content could not be parsed: " + e.getMessage();
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        // Split by paragraphs first
        String[] paragraphs = text.split("\n\\s*\n");

        StringBuilder currentChunk = new StringBuilder();
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            if (currentChunk.length() + trimmed.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                // Keep overlap from the end
                String overlap = getOverlap(currentChunk.toString(), chunkOverlap);
                currentChunk = new StringBuilder(overlap);
            }

            if (currentChunk.length() > 0) currentChunk.append("\n\n");
            currentChunk.append(trimmed);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private String getOverlap(String text, int overlapChars) {
        if (text.length() <= overlapChars) return text;
        int start = Math.max(text.length() - overlapChars, 0);
        // Try to break at a sentence
        String tail = text.substring(start);
        int sentenceBreak = tail.indexOf(". ");
        if (sentenceBreak > 0 && sentenceBreak < overlapChars / 2) {
            return tail.substring(sentenceBreak + 2);
        }
        return tail;
    }
}
