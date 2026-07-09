package com.smartagent.agent;

import com.smartagent.tool.Tool;
import com.smartagent.tool.ToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for all available tools.
 */
@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, Tool> tools = new HashMap<>();

    public void register(Tool tool) {
        String name = tool.getDefinition().getName();
        tools.put(name, tool);
        log.info("Registered tool: {}", name);
    }

    public Tool getTool(String name) {
        return tools.get(name);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    public List<ToolDefinition> getAllToolDefinitions() {
        return tools.values().stream()
                .map(Tool::getDefinition)
                .collect(Collectors.toList());
    }

    public int size() {
        return tools.size();
    }
}
