package com.smartagent.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartagent.ai.*;
import com.smartagent.tool.ToolDefinition;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Zhipu GLM AI Client using OpenAI-compatible API.
 * Docs: https://open.bigmodel.cn/dev/api/openapi/glm-4
 */
public class ZhipuAIClient implements AIClient {

    private static final Logger log = LoggerFactory.getLogger(ZhipuAIClient.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper;

    public ZhipuAIClient(String baseUrl, String apiKey, String model, int timeoutSeconds) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.mapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        request.setModel(model);
        String json = buildChatRequestJson(request, false);
        String response = executeChat(json);
        return parseChatResponse(response);
    }

    @Override
    public ChatResponse chatWithTools(ChatRequest request) {
        request.setModel(model);
        String json = buildChatRequestJson(request, true);
        String response = executeChat(json);
        return parseChatResponse(response);
    }

    @Override
    public List<Float> embed(String text) {
        try {
            ObjectNode body = mapper.createObjectNode()
                    .put("model", "embedding-2")
                    .put("input", text);

            Request httpReq = new Request.Builder()
                    .url(baseUrl + "/embeddings")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            String resp = execute(httpReq);
            JsonNode root = mapper.readTree(resp);
            JsonNode embedding = root.path("data").get(0).path("embedding");

            List<Float> result = new ArrayList<>();
            embedding.forEach(v -> result.add(v.floatValue()));
            return result;
        } catch (Exception e) {
            log.error("Embedding failed", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }

    private String buildChatRequestJson(ChatRequest request, boolean withTools) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("model", request.getModel());
            root.put("max_tokens", request.getMaxTokens());
            root.put("temperature", request.getTemperature());

            // Build messages array
            ArrayNode messages = root.putArray("messages");
            for (ChatMessage msg : request.getMessages()) {
                ObjectNode msgNode = messages.addObject();
                msgNode.put("role", msg.getRole().name());

                // Tool result messages
                if (msg.getRole() == ChatMessage.Role.tool) {
                    msgNode.put("content", msg.getContent());
                    msgNode.put("tool_call_id", msg.getToolCallId());
                    continue;
                }

                // Assistant messages with tool calls
                if (msg.getRole() == ChatMessage.Role.assistant
                        && msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
                    msgNode.put("content", msg.getContent() != null ? msg.getContent() : "");
                    ArrayNode toolCalls = msgNode.putArray("tool_calls");
                    for (ToolCall tc : msg.getToolCalls()) {
                        ObjectNode tcNode = toolCalls.addObject();
                        tcNode.put("id", tc.getId());
                        tcNode.put("type", "function");
                        ObjectNode func = tcNode.putObject("function");
                        func.put("name", tc.getFunction().getName());
                        func.put("arguments", tc.getFunction().getArguments());
                    }
                    continue;
                }

                // Regular messages
                msgNode.put("content", msg.getContent() != null ? msg.getContent() : "");
            }

            // Add tools if needed and present
            if (withTools && request.getTools() != null && !request.getTools().isEmpty()) {
                ArrayNode toolsArray = root.putArray("tools");
                for (ToolDefinition toolDef : request.getTools()) {
                    ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("type", "function");
                    ObjectNode func = toolNode.putObject("function");
                    func.put("name", toolDef.getName());
                    func.put("description", toolDef.getDescription());

                    // Parse the JSON schema for parameters
                    ObjectNode params;
                    try {
                        params = mapper.readValue(toolDef.getParameters(), ObjectNode.class);
                    } catch (Exception e) {
                        params = mapper.createObjectNode();
                    }
                    func.set("parameters", params);
                }
            }

            return root.toString();
        } catch (Exception e) {
            log.error("Failed to build request JSON", e);
            throw new RuntimeException("Failed to build request JSON", e);
        }
    }

    private String executeChat(String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();
        return execute(request);
    }

    private String execute(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("API request failed: status={}, body={}", response.code(), body);
                throw new RuntimeException("API request failed: " + response.code() + " - " + body);
            }
            return body;
        } catch (IOException e) {
            log.error("API request failed", e);
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    private ChatResponse parseChatResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            ChatResponse resp = new ChatResponse();

            // Model
            resp.setModel(root.path("model").asText());

            // Usage
            JsonNode usage = root.path("usage");
            resp.setInputTokens(usage.path("prompt_tokens").asInt());
            resp.setOutputTokens(usage.path("completion_tokens").asInt());

            // Choices
            JsonNode choice = root.path("choices").get(0);
            if (choice == null) return resp;

            JsonNode message = choice.path("message");
            resp.setContent(message.path("content").asText(""));
            resp.setFinishReason(choice.path("finish_reason").asText(""));

            // Tool calls
            JsonNode toolCalls = message.path("tool_calls");
            if (toolCalls.isArray() && toolCalls.size() > 0) {
                List<ToolCall> calls = new ArrayList<>();
                for (JsonNode tc : toolCalls) {
                    ToolCall call = new ToolCall();
                    call.setId(tc.path("id").asText());
                    call.setType(tc.path("type").asText());
                    ToolCall.Function func = new ToolCall.Function();
                    func.setName(tc.path("function").path("name").asText());
                    func.setArguments(tc.path("function").path("arguments").asText());
                    call.setFunction(func);
                    calls.add(call);
                }
                resp.setToolCalls(calls);
            }

            return resp;
        } catch (Exception e) {
            log.error("Failed to parse response JSON", e);
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}
