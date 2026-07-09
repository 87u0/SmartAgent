package com.smartagent.ai;

import java.util.List;
import java.util.Map;

/**
 * Unified chat message for all AI providers.
 */
public class ChatMessage {
    public enum Role {
        system, user, assistant, tool
    }

    private Role role;
    private String content;
    private List<ToolCall> toolCalls;
    private String toolCallId;
    private String name;

    public ChatMessage() {}

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(Role.system, content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(Role.user, content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(Role.assistant, content);
    }

    public static ChatMessage assistantWithTools(String content, List<ToolCall> toolCalls) {
        ChatMessage msg = new ChatMessage(Role.assistant, content);
        msg.toolCalls = toolCalls;
        return msg;
    }

    public static ChatMessage toolResult(String toolCallId, String name, String content) {
        ChatMessage msg = new ChatMessage(Role.tool, content);
        msg.toolCallId = toolCallId;
        msg.name = name;
        return msg;
    }

    // Getters and setters
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

    public String getToolCallId() { return toolCallId; }
    public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
