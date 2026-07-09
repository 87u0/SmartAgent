package com.smartagent.web.dto;

import java.util.List;

/**
 * Chat request DTO from the web frontend.
 */
public class ChatRequestDTO {
    private String message;
    private boolean enableTools = true;
    private boolean enableRag = false;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isEnableTools() { return enableTools; }
    public void setEnableTools(boolean enableTools) { this.enableTools = enableTools; }

    public boolean isEnableRag() { return enableRag; }
    public void setEnableRag(boolean enableRag) { this.enableRag = enableRag; }
}
