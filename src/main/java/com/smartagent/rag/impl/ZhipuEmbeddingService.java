package com.smartagent.rag.impl;

import com.smartagent.ai.AIClient;
import com.smartagent.rag.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedding service using Zhipu's embedding API.
 */
@Service
public class ZhipuEmbeddingService implements EmbeddingService {

    private final AIClient aiClient;

    public ZhipuEmbeddingService(AIClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public List<Float> embed(String text) {
        return aiClient.embed(text);
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        List<List<Float>> results = new ArrayList<>();
        for (String text : texts) {
            results.add(aiClient.embed(text));
        }
        return results;
    }
}
