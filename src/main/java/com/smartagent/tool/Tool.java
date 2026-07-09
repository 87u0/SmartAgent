package com.smartagent.tool;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tool interface for the Agent to execute.
 */
public interface Tool {

    /**
     * Execute the tool with given arguments.
     * @param args JSON arguments from AI model
     * @return result string
     */
    String execute(JsonNode args);

    /**
     * Get the tool definition for AI model function calling.
     */
    ToolDefinition getDefinition();
}
