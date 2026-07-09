package com.smartagent.web.controller;

import com.smartagent.agent.Agent;
import com.smartagent.agent.AgentResult;
import com.smartagent.agent.ToolRegistry;
import com.smartagent.rag.DocumentService;
import com.smartagent.web.dto.ChatRequestDTO;
import com.smartagent.web.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Chat controller - handles Agent conversations.
 */
@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final Agent agent;
    private final DocumentService documentService;
    private final ToolRegistry toolRegistry;

    public ChatController(Agent agent, DocumentService documentService,
                          ToolRegistry toolRegistry) {
        this.agent = agent;
        this.documentService = documentService;
        this.toolRegistry = toolRegistry;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/knowledge")
    public String knowledgePage() {
        return "knowledge";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ChatResponseDTO chat(@RequestBody ChatRequestDTO request) {
        log.info("Chat request: {} (tools={}, rag={})",
                request.getMessage(), request.isEnableTools(), request.isEnableRag());

        String context = null;
        if (request.isEnableRag()) {
            context = documentService.searchContext(request.getMessage(), 5);
            if (context.isEmpty()) context = null;
        }

        AgentResult result = agent.execute(request.getMessage(), context);

        return new ChatResponseDTO(
                result.getTaskResponse(),
                result.getIterations(),
                request.isEnableTools(),
                request.isEnableRag());
    }

    @GetMapping("/api/tools")
    @ResponseBody
    public Map<String, Object> listTools() {
        return Map.of(
                "count", toolRegistry.size(),
                "tools", toolRegistry.getAllToolDefinitions().stream()
                        .map(t -> Map.of("name", t.getName(), "description", t.getDescription()))
                        .toList()
        );
    }
}
