package com.ximi.huiaiagent.knowledge.loader;

import com.ximi.huiaiagent.knowledge.config.KnowledgeProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LocalFileLoader {

    @Resource(name = "knowledgeProperties")
    private KnowledgeProperties knowledgeProperties;

    public List<Document> loadLocalMarkdownFiles() {
        List<Document> allDocuments = new ArrayList<>();
        
        String basePath = knowledgeProperties.getBasePath();
        if (basePath == null || basePath.isEmpty()) {
            log.warn("知识库路径未配置");
            return allDocuments;
        }
        
        File directory = new File(basePath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("知识库目录不存在: {}", basePath);
            return allDocuments;
        }
        
        File[] mdFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".md"));
        if (mdFiles == null || mdFiles.length == 0) {
            log.info("未找到md文件: {}", basePath);
            return allDocuments;
        }
        
        for (File file : mdFiles) {
            try {
                List<Document> documents = loadMarkdownFile(file);
                allDocuments.addAll(documents);
                log.info("加载文件成功: {}", file.getName());
            } catch (Exception e) {
                log.error("加载文件失败: {}", file.getName(), e);
            }
        }
        
        return allDocuments;
    }

    private List<Document> loadMarkdownFile(File file) throws IOException {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("filename", file.getName())
                .withAdditionalMetadata("fileType", "md")
                .withAdditionalMetadata("filePath", file.getAbsolutePath())
                .withAdditionalMetadata("fileId", generateFileId(file))
                .build();

        FileSystemResource fileSystemResource = new FileSystemResource(file);
        MarkdownDocumentReader reader = new MarkdownDocumentReader(fileSystemResource, config);
        return reader.get();
    }

    private String generateFileId(File file) {
        return UUID.nameUUIDFromBytes(file.getAbsolutePath().getBytes()).toString();
    }

    public Optional<File[]> getLocalMarkdownFiles() {
        String basePath = knowledgeProperties.getBasePath();
        if (basePath == null || basePath.isEmpty()) {
            return Optional.empty();
        }
        
        File directory = new File(basePath);
        if (!directory.exists() || !directory.isDirectory()) {
            return Optional.empty();
        }
        
        File[] mdFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".md"));
        return Optional.ofNullable(mdFiles);
    }
}
