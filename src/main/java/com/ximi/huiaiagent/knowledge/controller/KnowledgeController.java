package com.ximi.huiaiagent.knowledge.controller;

import com.ximi.huiaiagent.knowledge.model.DocumentMeta;
import com.ximi.huiaiagent.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "文档上传、列表、同步、删除接口")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "上传PDF/Word/Excel/MD文档到知识库")
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        KnowledgeService.UploadResult result = knowledgeService.uploadDocument(file);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.getDocumentMeta() != null) {
            response.put("data", result.getDocumentMeta());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "获取文档列表", description = "获取知识库中所有文档")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        List<DocumentMeta> documents = knowledgeService.listDocuments();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", documents);
        response.put("total", documents.size());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步本地目录", description = "同步配置目录下的所有.md文件到知识库")
    public ResponseEntity<Map<String, Object>> syncLocalDirectory() {
        KnowledgeService.SyncResult result = knowledgeService.syncLocalDirectory();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", result.getMessage());
        response.put("count", result.getCount());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档", description = "根据ID删除知识库中的文档")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id) {
        boolean success = knowledgeService.deleteDocument(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败，文档不存在");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    @Operation(summary = "获取配置信息", description = "获取知识库配置信息")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("basePath", knowledgeService.getBasePath());
        
        return ResponseEntity.ok(response);
    }
}
