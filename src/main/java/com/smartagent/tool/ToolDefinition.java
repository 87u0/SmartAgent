package com.smartagent.tool;

/**
 * Tool definition sent to the AI model for function calling.
 */
public class ToolDefinition {
    private String name;
    private String description;
    private String parameters; // JSON schema string

    public ToolDefinition() {}

    public ToolDefinition(String name, String description, String parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}
