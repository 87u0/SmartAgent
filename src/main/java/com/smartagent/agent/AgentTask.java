package com.smartagent.agent;

import lombok.Data;

/**
 * Represents a task for the agent to execute.
 */
@Data
public class AgentTask {
    private final String id;
    private final String instruction;
    private long createdAt;

    public AgentTask(String instruction) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.instruction = instruction;
        this.createdAt = System.currentTimeMillis();
    }
}
