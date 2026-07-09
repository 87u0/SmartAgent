package com.smartagent.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartagent.tool.Tool;
import com.smartagent.tool.ToolDefinition;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;

/**
 * Calculator tool - evaluates mathematical expressions safely.
 */
@Component
public class CalculatorTool implements Tool {

    @Override
    public String execute(JsonNode args) {
        String expression = args.has("expression") ? args.get("expression").asText() : "";

        if (expression.isEmpty()) {
            return "Error: expression is required";
        }

        // Sanitize: only allow basic math characters
        if (!expression.matches("[0-9+\\-*/.()\\s]+")) {
            return "Error: expression contains invalid characters";
        }

        try {
            // Simple evaluation using JavaScript engine
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            return "Error evaluating expression: " + e.getMessage();
        }
    }

    @Override
    public ToolDefinition getDefinition() {
        return new ToolDefinition(
                "calculator",
                "Evaluate mathematical expressions (e.g., 2 + 3 * 4, sqrt(16))",
                """
                {
                    "type": "object",
                    "properties": {
                        "expression": {
                            "type": "string",
                            "description": "Mathematical expression to evaluate"
                        }
                    },
                    "required": ["expression"]
                }
                """
        );
    }
}
