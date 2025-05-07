package com.aicodereview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {
    private String provider;
    private OpenAIConfig openai;
    private AnthropicConfig anthropic;
    private AzureConfig azure;

    @Data
    public static class OpenAIConfig {
        private ApiConfig api;
    }

    @Data
    public static class AnthropicConfig {
        private ApiConfig api;
    }

    @Data
    public static class AzureConfig {
        private ApiConfig api;
    }

    @Data
    public static class ApiConfig {
        private String key;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Integer timeout;
        private String endpoint;
        private String deploymentName;
        private String apiVersion;
    }
} 