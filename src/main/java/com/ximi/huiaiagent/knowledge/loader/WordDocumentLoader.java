package com.ximi.huiaiagent.knowledge.loader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WordDocumentLoader implements DocumentLoader {

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> load(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            
            StringBuilder fullText = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    fullText.append(text).append("\n");
                }
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("fileType", "docx");
            metadata.put("paragraphCount", paragraphs.size());
            
            Document doc = Document.builder()
                    .id(file.getOriginalFilename())
                    .text(fullText.toString())
                    .metadata(metadata)
                    .build();
            
            documents.add(doc);
            
        } catch (Exception e) {
            log.error("Word解析失败: {}", file.getOriginalFilename(), e);
            throw new IOException("Word解析失败", e);
        }
        
        return documents;
    }
}
