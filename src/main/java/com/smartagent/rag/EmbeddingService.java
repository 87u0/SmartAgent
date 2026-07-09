package com.smartagent.rag;

import java.util.List;

/**
 * Embedding service for converting text to vector representations.
 */
public interface EmbeddingService {

    /**
     * Create embedding vector for a single text.
     */
    List<Float> embed(String text);

    /**
     * Create embeddings for multiple texts in batch.
     */
    List<List<Float>> embedBatch(List<String> texts);
}
