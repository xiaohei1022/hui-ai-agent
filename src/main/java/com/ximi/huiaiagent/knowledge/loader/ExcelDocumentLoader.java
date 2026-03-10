package com.ximi.huiaiagent.knowledge.loader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
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
public class ExcelDocumentLoader implements DocumentLoader {

    @Override
    public boolean supports(String fileType) {
        return "xlsx".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> load(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            StringBuilder fullText = new StringBuilder();
            int sheetCount = workbook.getNumberOfSheets();
            
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                fullText.append("Sheet: ").append(sheetName).append("\n");
                
                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (cellValue != null && !cellValue.isEmpty()) {
                            rowText.append(cellValue).append("\t");
                        }
                    }
                    if (rowText.length() > 0) {
                        fullText.append(rowText).append("\n");
                    }
                }
                fullText.append("\n");
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("fileType", "xlsx");
            metadata.put("sheetCount", sheetCount);
            
            Document doc = Document.builder()
                    .id(file.getOriginalFilename())
                    .text(fullText.toString())
                    .metadata(metadata)
                    .build();
            
            documents.add(doc);
            
        } catch (Exception e) {
            log.error("Excel解析失败: {}", file.getOriginalFilename(), e);
            throw new IOException("Excel解析失败", e);
        }
        
        return documents;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
