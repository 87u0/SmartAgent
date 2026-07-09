package com.smartagent.ai;

import com.smartagent.tool.ToolDefinition;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Unified chat request for all AI providers.
 */
@Data
@Accessors(chain = true)
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private List<ToolDefinition> tools;
    private int maxTokens = 4096;
    private double temperature = 0.7;
}
