package com.aicodereview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm.ollama")
public class OllamaConfig {
    private ApiConfig api;

    @Data
    public static class ApiConfig {
        private String url;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Integer timeout;
    }
}