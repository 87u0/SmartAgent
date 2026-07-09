package com.smartagent.config;

import com.smartagent.agent.Agent;
import com.smartagent.ai.AIClient;
import com.smartagent.ai.impl.ZhipuAIClient;
import com.smartagent.agent.ToolRegistry;
import com.smartagent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    @Value("${smartagent.ai.zhipu.api-key}")
    private String apiKey;

    @Value("${smartagent.ai.zhipu.model}")
    private String model;

    @Value("${smartagent.ai.zhipu.base-url}")
    private String baseUrl;

    @Value("${smartagent.ai.timeout}")
    private int timeout;

    @Value("${smartagent.agent.max-iterations:10}")
    private int maxIterations;

    @Bean
    public AIClient aiClient() {
        return new ZhipuAIClient(baseUrl, apiKey, model, timeout);
    }

    @Bean
    public ToolRegistry toolRegistry(List<Tool> tools) {
        ToolRegistry registry = new ToolRegistry();
        tools.forEach(registry::register);
        return registry;
    }

    @Bean
    public Agent agent(AIClient aiClient, ToolRegistry toolRegistry) {
        return new Agent(aiClient, toolRegistry, maxIterations);
    }
}
