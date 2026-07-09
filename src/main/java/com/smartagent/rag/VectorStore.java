package com.smartagent.rag;

import java.util.List;

/**
 * Vector store interface for storing and searching document embeddings.
 */
public interface VectorStore {

    /**
     * Add a document chunk to the vector store.
     */
    void addDocument(String id, String content, List<Float> embedding);

    /**
     * Search for similar documents by embedding vector.
     */
    List<SearchResult> search(List<Float> queryEmbedding, int topK);

    /**
     * Get all stored documents.
     */
    List<StoredDocument> getAllDocuments();

    /**
     * Delete a document by ID.
     */
    void deleteDocument(String id);

    /**
     * Clear all documents.
     */
    void clear();

    record SearchResult(String id, String content, double score) {}
    record StoredDocument(String id, String content) {}
}
