package com.ximi.huiaiagent.knowledge.repository;

import com.ximi.huiaiagent.knowledge.model.DocumentMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentMeta, Long> {

    Optional<DocumentMeta> findByFileId(String fileId);

    Optional<DocumentMeta> findByFileName(String fileName);
}
