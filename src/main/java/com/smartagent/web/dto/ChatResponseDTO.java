package com.smartagent.web.dto;

/**
 * Chat response DTO sent to the web frontend.
 */
public class ChatResponseDTO {
    private String response;
    private int iterations;
    private boolean usedTools;
    private boolean usedRag;

    public ChatResponseDTO(String response, int iterations, boolean usedTools, boolean usedRag) {
        this.response = response;
        this.iterations = iterations;
        this.usedTools = usedTools;
        this.usedRag = usedRag;
    }

    public String getResponse() { return response; }
    public int getIterations() { return iterations; }
    public boolean isUsedTools() { return usedTools; }
    public boolean isUsedRag() { return usedRag; }
}
