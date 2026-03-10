package com.ximi.huiaiagent.knowledge.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DocumentLoaderFactory {

    private final Map<String, DocumentLoader> loaders;

    @Autowired
    public DocumentLoaderFactory(List<DocumentLoader> loaderList) {
        this.loaders = loaderList.stream()
                .collect(Collectors.toMap(
                        loader -> getSupportedType(loader.getClass()),
                        Function.identity()
                ));
    }

    private String getSupportedType(Class<? extends DocumentLoader> clazz) {
        if (clazz == MarkdownDocumentLoader.class) return "md";
        if (clazz == PdfDocumentLoader.class) return "pdf";
        if (clazz == WordDocumentLoader.class) return "docx";
        if (clazz == ExcelDocumentLoader.class) return "xlsx";
        return clazz.getSimpleName().replace("DocumentLoader", "").toLowerCase();
    }

    public DocumentLoader getLoader(String fileType) {
        DocumentLoader loader = loaders.get(fileType.toLowerCase());
        if (loader == null) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
        return loader;
    }

    public List<Document> load(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = fileName != null ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
        return getLoader(fileType).load(file);
    }
}
