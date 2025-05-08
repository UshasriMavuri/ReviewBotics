package com.aicodereview.config;

import com.aicodereview.service.LLMService;
import com.aicodereview.service.impl.OllamaServiceImpl;
import com.aicodereview.service.impl.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class LLMServiceConfig {

    private final LLMConfig llmConfig;
    private final RestTemplate restTemplate;

    @Bean
    @Primary
    public LLMService llmService() {
        String provider = llmConfig.getProvider();
        return switch (provider.toLowerCase()) {
            case "ollama" -> new OllamaServiceImpl(restTemplate, null);
            case "openai" -> new OpenAIService(llmConfig);
            default -> throw new IllegalStateException("Unsupported LLM provider: " + provider);
        };
    }
} 