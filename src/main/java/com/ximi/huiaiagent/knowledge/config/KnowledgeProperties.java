package com.ximi.huiaiagent.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {

    private String basePath;
    private List<String> supportedTypes;
    private int chunkSize = 500;
    private int chunkOverlap = 50;
    private String uploadDir = "./uploads";
    private int fileRetentionDays = 30;
}
