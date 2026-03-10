package com.ximi.huiaiagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VectorStoreConfig {

    @Autowired
    private EmbeddingModel dashscopeEmbeddingModel;

    @Bean
    @Primary
    public VectorStore knowledgeVectorStore() {
        return SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
    }
}
