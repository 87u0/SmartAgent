package com.smartagent.ai;

import java.util.List;

/**
 * Unified AI client interface.
 * Supports multiple AI providers through this abstraction.
 */
public interface AIClient {

    /**
     * Send a chat completion request.
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Send a chat completion with tool support.
     */
    ChatResponse chatWithTools(ChatRequest request);

    /**
     * Create embeddings for text.
     */
    List<Float> embed(String text);
}
