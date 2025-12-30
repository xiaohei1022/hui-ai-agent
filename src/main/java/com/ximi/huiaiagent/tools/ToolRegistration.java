package com.ximi.huiaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        List<ToolCallback> toolCallbacks = new ArrayList<>();

        // 分别尝试注册每个工具
        addToolSafely(toolCallbacks, new FileOperationTool());
        addToolSafely(toolCallbacks, new WebSearchTool(searchApiKey));
        addToolSafely(toolCallbacks, new WebScrapingTool());
        addToolSafely(toolCallbacks, new ResourceDownloadTool());
        addToolSafely(toolCallbacks, new TerminalOperationTool());
        addToolSafely(toolCallbacks, new PDFGenerationTool());
        addToolSafely(toolCallbacks, new TerminateTool());

        return toolCallbacks.toArray(new ToolCallback[0]);
    }

    private void addToolSafely(List<ToolCallback> list, Object tool) {
        try {
            ToolCallback[] callbacks = ToolCallbacks.from(tool);
            list.addAll(Arrays.asList(callbacks));
        } catch (Exception e) {
            System.err.println("工具注册失败: " + tool.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
