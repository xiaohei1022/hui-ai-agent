package com.ximi.huiaiagent.knowledge.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
public class MarkdownDocumentLoader implements DocumentLoader {

    @Override
    public boolean supports(String fileType) {
        return "md".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> load(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(false)
                    .withAdditionalMetadata("filename", file.getOriginalFilename())
                    .withAdditionalMetadata("fileType", "md")
                    .build();

            Resource resource = new InputStreamResource(file.getInputStream());
            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
            return reader.get();
        } catch (Exception e) {
            log.error("Markdown解析失败: {}", file.getOriginalFilename(), e);
            throw new IOException("Markdown解析失败", e);
        }
    }
}
