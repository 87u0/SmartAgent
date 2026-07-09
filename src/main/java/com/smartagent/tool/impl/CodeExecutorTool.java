package com.smartagent.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartagent.tool.Tool;
import com.smartagent.tool.ToolDefinition;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * Code executor tool - safely executes Python code snippets.
 */
@Component
public class CodeExecutorTool implements Tool {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public String execute(JsonNode args) {
        String language = args.has("language") ? args.get("language").asText() : "python";
        String code = args.has("code") ? args.get("code").asText() : "";

        if (code.isEmpty()) {
            return "Error: code is required";
        }

        if (!language.equals("python")) {
            return "Error: only Python is supported currently";
        }

        // Security: block dangerous imports
        String lower = code.toLowerCase();
        String[] blocked = {"import os", "import subprocess", "import sys",
                "import shutil", "__import__", "eval(", "exec(", "open("};
        for (String b : blocked) {
            if (lower.contains(b)) {
                return "Error: use of '" + b + "' is not allowed for security";
            }
        }

        try {
            // Write code to temp file and execute
            Path tempFile = Files.createTempFile("code_", ".py");
            Files.writeString(tempFile, code);

            Future<String> future = executor.submit(() -> {
                ProcessBuilder pb = new ProcessBuilder("python", tempFile.toString());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // Timeout
                if (!process.waitFor(15, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    return "Error: execution timed out (15s)";
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                // Clean up
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}

                return output.toString().trim();
            });

            String result = future.get(20, TimeUnit.SECONDS);
            return result.isEmpty() ? "Code executed successfully (no output)" : result;

        } catch (TimeoutException e) {
            return "Error: execution timed out";
        } catch (Exception e) {
            return "Error executing code: " + e.getMessage();
        }
    }

    @Override
    public ToolDefinition getDefinition() {
        return new ToolDefinition(
                "code_executor",
                "Execute Python code and return the output. Use for data analysis, calculations, or generating content.",
                """
                {
                    "type": "object",
                    "properties": {
                        "language": {
                            "type": "string",
                            "description": "Programming language (only python supported)"
                        },
                        "code": {
                            "type": "string",
                            "description": "Python code to execute"
                        }
                    },
                    "required": ["language", "code"]
                }
                """
        );
    }
}
