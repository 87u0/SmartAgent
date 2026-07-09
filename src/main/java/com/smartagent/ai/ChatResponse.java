package com.smartagent.ai;

import lombok.Data;

import java.util.List;

/**
 * Unified chat response for all AI providers.
 */
@Data
public class ChatResponse {
    private String content;
    private List<ToolCall> toolCalls;
    private String model;
    private int inputTokens;
    private int outputTokens;
    private String finishReason;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
