package com.smartagent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartagent.ai.*;
import com.smartagent.tool.Tool;
import com.smartagent.tool.ToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core ReAct (Reasoning + Acting) Agent.
 * Implements the Thought → Action → Observation loop.
 */
public class Agent {

    private static final Logger log = LoggerFactory.getLogger(Agent.class);

    private final AIClient aiClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String systemPrompt;

    private int maxIterations = 10;
    private int maxToolRetries = 3;

    public Agent(AIClient aiClient, ToolRegistry toolRegistry) {
        this.aiClient = aiClient;
        this.toolRegistry = toolRegistry;
        this.systemPrompt = buildSystemPrompt();
    }

    public Agent(AIClient aiClient, ToolRegistry toolRegistry, int maxIterations) {
        this(aiClient, toolRegistry);
        this.maxIterations = maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setMaxToolRetries(int maxToolRetries) {
        this.maxToolRetries = maxToolRetries;
    }

    /**
     * Execute a task with the ReAct loop and return the final response.
     */
    public AgentResult execute(String instruction) {
        return execute(instruction, null);
    }

    /**
     * Execute with context (RAG results, etc.).
     */
    public AgentResult execute(String instruction, String context) {
        log.info("Agent starting task: {}", instruction);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));

        // Add context if provided
        if (context != null && !context.isEmpty()) {
            messages.add(ChatMessage.system("Relevant context:\n" + context));
        }

        messages.add(ChatMessage.user(instruction));

        List<ToolDefinition> toolDefs = toolRegistry.getAllToolDefinitions();
        int iteration = 0;
        StringBuffer fullResponse = new StringBuffer();

        while (iteration < maxIterations) {
            iteration++;
            log.debug("Agent iteration {}/{}", iteration, maxIterations);

            ChatRequest request = new ChatRequest()
                    .setMessages(messages)
                    .setTools(toolDefs);

            ChatResponse response = aiClient.chatWithTools(request);

            if (response.getContent() != null && !response.getContent().isEmpty()) {
                fullResponse.append(response.getContent()).append("\n");
            }

            // No tool calls → agent is done
            if (!response.hasToolCalls()) {
                messages.add(ChatMessage.assistant(response.getContent()));
                break;
            }

            // Add assistant message with tool calls
            messages.add(ChatMessage.assistantWithTools(
                    response.getContent(), response.getToolCalls()));

            // Execute each tool call
            boolean allToolsSucceeded = true;
            for (ToolCall toolCall : response.getToolCalls()) {
                String toolName = toolCall.getFunction().getName();
                String args = toolCall.getFunction().getArguments();
                String toolCallId = toolCall.getId();

                log.info("Agent calling tool: {} with args: {}", toolName, args);

                if (!toolRegistry.hasTool(toolName)) {
                    messages.add(ChatMessage.toolResult(
                            toolCallId, toolName, "Error: Unknown tool '" + toolName + "'"));
                    allToolsSucceeded = false;
                    continue;
                }

                // Execute with retries
                String result = executeWithRetry(toolName, args, toolCallId, messages);
                messages.add(ChatMessage.toolResult(toolCallId, toolName, result));
                log.debug("Tool {} result (first 200 chars): {}", toolName,
                        result.substring(0, Math.min(200, result.length())));
            }
        }

        AgentResult result = new AgentResult();
        result.setTaskResponse(fullResponse.toString().trim());
        result.setIterations(iteration);

        if (iteration >= maxIterations) {
            result.setFinished(true);
            log.warn("Agent reached max iterations ({})", maxIterations);
        } else {
            result.setFinished(false);
        }

        log.info("Agent completed task in {} iterations", iteration);
        return result;
    }

    private String executeWithRetry(String toolName, String args,
                                     String toolCallId, List<ChatMessage> messages) {
        int retries = 0;
        while (retries < maxToolRetries) {
            try {
                Tool tool = toolRegistry.getTool(toolName);
                JsonNode argsJson = mapper.readTree(args);
                return tool.execute(argsJson);
            } catch (Exception e) {
                retries++;
                log.warn("Tool {} failed (retry {}/{}): {}",
                        toolName, retries, maxToolRetries, e.getMessage());
                if (retries >= maxToolRetries) {
                    return "Error after " + retries + " retries: " + e.getMessage();
                }
            }
        }
        return "Error: Tool execution failed";
    }

    private String buildSystemPrompt() {
        return """
                You are an intelligent AI assistant with access to tools.
                You follow the ReAct (Reasoning + Acting) pattern:
                1. Analyze the user's request
                2. Decide which tools to use
                3. Use tools to gather information
                4. Provide a final answer based on tool results

                Rules:
                - Use tools when you need external information
                - If a tool returns an error, try a different approach
                - Be concise but thorough in your responses
                - When you have enough information, provide the final answer
                - Think step by step for complex problems
                """;
    }
}
