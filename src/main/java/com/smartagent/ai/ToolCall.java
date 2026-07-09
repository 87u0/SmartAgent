package com.smartagent.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a tool call from AI model.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolCall {
    private String id;
    private String type = "function";
    private Function function;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Function {
        private String name;
        private String arguments; // JSON string
    }

    public ToolCall(String id, String name, String arguments) {
        this.id = id;
        this.function = new Function();
        this.function.name = name;
        this.function.arguments = arguments;
    }
}
