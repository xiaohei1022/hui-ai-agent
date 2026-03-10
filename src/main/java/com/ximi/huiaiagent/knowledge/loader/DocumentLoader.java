package com.ximi.huiaiagent.knowledge.loader;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentLoader {

    boolean supports(String fileType);

    List<Document> load(MultipartFile file) throws IOException;
}
