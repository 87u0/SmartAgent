package com.smartagent.rag.impl;

import com.smartagent.rag.VectorStore;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory vector store using cosine similarity.
 * For production, replace with ChromaDB / PGVector / Milvus.
 */
@Component
public class LocalVectorStore implements VectorStore {

    private final List<DocumentEntry> documents = new CopyOnWriteArrayList<>();

    @Override
    public void addDocument(String id, String content, List<Float> embedding) {
        documents.add(new DocumentEntry(id, content, embedding));
    }

    @Override
    public List<SearchResult> search(List<Float> queryEmbedding, int topK) {
        if (documents.isEmpty() || queryEmbedding == null) {
            return List.of();
        }

        return documents.parallelStream()
                .map(doc -> new SearchResult(
                        doc.id, doc.content,
                        cosineSimilarity(queryEmbedding, doc.embedding)))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoredDocument> getAllDocuments() {
        return documents.stream()
                .map(d -> new StoredDocument(d.id, d.content))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String id) {
        documents.removeIf(d -> d.id.equals(id));
    }

    @Override
    public void clear() {
        documents.clear();
    }

    private double cosineSimilarity(List<Float> a, List<Float> b) {
        if (a.size() != b.size() || a.isEmpty()) return 0;

        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : dotProduct / denom;
    }

    private record DocumentEntry(String id, String content, List<Float> embedding) {}
}
