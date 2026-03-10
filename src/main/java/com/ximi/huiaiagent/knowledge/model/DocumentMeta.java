package com.ximi.huiaiagent.knowledge.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "document_meta")
public class DocumentMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "file_id", unique = true)
    private String fileId;

    @Column(name = "preview_html", columnDefinition = "TEXT")
    private String previewHtml;
}
