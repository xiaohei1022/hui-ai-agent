package com.ximi.huiaiagent.knowledge.loader;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
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
public class PdfDocumentLoader implements DocumentLoader {

    @Override
    public boolean supports(String fileType) {
        return "pdf".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> load(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            PdfReader reader = new PdfReader(inputStream);
            PdfDocument pdfDoc = new PdfDocument(reader);
            
            StringBuilder fullText = new StringBuilder();
            int numberOfPages = pdfDoc.getNumberOfPages();
            
            for (int i = 1; i <= numberOfPages; i++) {
                String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                if (pageText != null && !pageText.trim().isEmpty()) {
                    fullText.append(pageText).append("\n\n");
                }
            }
            
            pdfDoc.close();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("fileType", "pdf");
            metadata.put("pageCount", numberOfPages);
            String docId = java.util.UUID.nameUUIDFromBytes(
                    (file.getOriginalFilename() + System.currentTimeMillis()).getBytes()
            ).toString();

            Document document = Document.builder()
                    .id(docId)
                    .text(fullText.toString())
                    .metadata(metadata)
                    .build();
            
            documents.add(document);
            
        } catch (Exception e) {
            log.error("PDF解析失败: {}", file.getOriginalFilename(), e);
            throw new IOException("PDF解析失败", e);
        }
        
        return documents;
    }
}
