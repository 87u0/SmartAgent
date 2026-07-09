package com.smartagent.agent;

import java.util.*;

/**
 * Simple in-memory conversation memory.
 * Stores conversation history for the agent.
 */
public class Memory {

    private final int maxMessages;
    private final List<MemoryMessage> messages = new ArrayList<>();

    public Memory() {
        this(50);
    }

    public Memory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public void add(String role, String content) {
        messages.add(new MemoryMessage(role, content));
        trim();
    }

    public void addToolMessage(String toolName, String content) {
        messages.add(new MemoryMessage("tool_result", "[" + toolName + "] " + content));
        trim();
    }

    public List<MemoryMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }

    private void trim() {
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public static class MemoryMessage {
        private final String role;
        private final String content;

        public MemoryMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
