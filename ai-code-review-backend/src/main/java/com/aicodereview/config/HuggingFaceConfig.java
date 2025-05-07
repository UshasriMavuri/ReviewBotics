package com.aicodereview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm.huggingface")
public class HuggingFaceConfig {
    private ApiConfig api;

    @Data
    public static class ApiConfig {
        private String key;
        private String model;
        private String endpoint;
        private Double temperature;
        private Integer maxTokens;
        private Integer timeout;
    }
} 