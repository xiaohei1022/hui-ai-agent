package com.ximi.huiaiagent.knowledge.service;

import com.ximi.huiaiagent.knowledge.config.KnowledgeProperties;
import com.ximi.huiaiagent.knowledge.loader.DocumentLoaderFactory;
import com.ximi.huiaiagent.knowledge.loader.LocalFileLoader;
import com.ximi.huiaiagent.knowledge.model.DocumentMeta;
import com.ximi.huiaiagent.knowledge.repository.DocumentRepository;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class KnowledgeService {

    @Resource
    private DocumentLoaderFactory documentLoaderFactory;

    @Resource
    private LocalFileLoader localFileLoader;

    @Resource
    private DocumentRepository documentRepository;

    @Resource
    private VectorStore knowledgeVectorStore;

    @Resource
    private KnowledgeProperties knowledgeProperties;

    @Data
    public static class UploadResult {
        private boolean success;
        private String message;
        private DocumentMeta documentMeta;

        public static UploadResult success(DocumentMeta meta, String message) {
            UploadResult result = new UploadResult();
            result.setSuccess(true);
            result.setMessage(message);
            result.setDocumentMeta(meta);
            return result;
        }

        public static UploadResult fail(String message) {
            UploadResult result = new UploadResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    @Transactional
    public UploadResult uploadDocument(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                return UploadResult.fail("文件名不能为空");
            }

            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!isSupportedType(fileType)) {
                return UploadResult.fail("不支持的文件类型: " + fileType);
            }

            List<Document> documents = documentLoaderFactory.load(file);
            if (documents.isEmpty()) {
                return UploadResult.fail("文档解析失败或文档为空");
            }

            String contentText = documents.stream()
                    .map(Document::getText)
                    .reduce("", String::concat);

            String fileId = UUID.nameUUIDFromBytes((file.getOriginalFilename()).getBytes()).toString();
            
            DocumentMeta meta = documentRepository.findByFileId(fileId).orElse(new DocumentMeta());
            meta.setFileId(fileId);
            meta.setFileName(file.getOriginalFilename());
            meta.setFileType(fileType);
            meta.setContentText(contentText);
            meta.setChunkCount(documents.size());
            meta.setCreatedAt(LocalDateTime.now());
            meta.setFilePath("upload");

            DocumentMeta savedMeta = documentRepository.save(meta);

            knowledgeVectorStore.add(documents);
            log.info("文档上传成功: {}, chunk数量: {}", fileName, documents.size());

            return UploadResult.success(savedMeta, "上传成功");

        } catch (IOException e) {
            log.error("文档上传失败", e);
            return UploadResult.fail("文档解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return UploadResult.fail("上传失败: " + e.getMessage());
        }
    }

    public List<DocumentMeta> listDocuments() {
        return documentRepository.findAll();
    }

    @Transactional
    public boolean deleteDocument(Long id) {
        Optional<DocumentMeta> optionalMeta = documentRepository.findById(id);
        if (optionalMeta.isEmpty()) {
            return false;
        }

        DocumentMeta meta = optionalMeta.get();

        Document filterDocument = Document.builder()
                .id(meta.getFileName())
                .text("")
                .metadata(java.util.Map.of("filename", meta.getFileName()))
                .build();

        try {
            knowledgeVectorStore.delete(List.of(filterDocument.getId()));
        } catch (Exception e) {
            log.warn("向量删除失败，尝试按元数据删除", e);
        }

        documentRepository.delete(meta);
        log.info("文档删除成功: {}", meta.getFileName());
        return true;
    }

    @Transactional
    public SyncResult syncLocalDirectory() {
        List<Document> documents = localFileLoader.loadLocalMarkdownFiles();
        
        if (documents.isEmpty()) {
            return new SyncResult(0, "未找到需要同步的文档");
        }

        for (Document doc : documents) {
            String fileId = (String) doc.getMetadata().getOrDefault("fileId", 
                    UUID.nameUUIDFromBytes(doc.getText().getBytes()).toString());
            String fileName = (String) doc.getMetadata().get("filename");
            
            DocumentMeta meta = documentRepository.findByFileId(fileId)
                    .orElseGet(DocumentMeta::new);
            
            meta.setFileId(fileId);
            meta.setFileName(fileName);
            meta.setFileType("md");
            meta.setContentText(doc.getText());
            meta.setChunkCount(1);
            meta.setCreatedAt(LocalDateTime.now());
            meta.setFilePath((String) doc.getMetadata().get("filePath"));
            
            documentRepository.save(meta);
        }

        knowledgeVectorStore.add(documents);
        
        log.info("本地目录同步成功，共 {} 个文档", documents.size());
        return new SyncResult(documents.size(), "同步成功");
    }

    public String getBasePath() {
        return knowledgeProperties.getBasePath();
    }

    private boolean isSupportedType(String fileType) {
        List<String> supportedTypes = knowledgeProperties.getSupportedTypes();
        if (supportedTypes == null) {
            return false;
        }
        return supportedTypes.contains(fileType.toLowerCase());
    }

    @Data
    public static class SyncResult {
        private int count;
        private String message;

        public SyncResult(int count, String message) {
            this.count = count;
            this.message = message;
        }
    }
}
