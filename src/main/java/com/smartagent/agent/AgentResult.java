package com.smartagent.agent;

/**
 * Result of an agent execution.
 */
public class AgentResult {
    private String taskResponse;
    private int iterations;
    private boolean finished;

    public String getTaskResponse() { return taskResponse; }
    public void setTaskResponse(String taskResponse) { this.taskResponse = taskResponse; }

    public int getIterations() { return iterations; }
    public void setIterations(int iterations) { this.iterations = iterations; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
}
