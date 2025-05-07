package com.aicodereview.service.impl;

import com.aicodereview.config.LLMConfig;
import com.aicodereview.service.LLMService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService implements LLMService {

    private final LLMConfig llmConfig;
    private OpenAiService openAiService;

    @Override
    public String generateCodeReview(String code, String context) {
        try {
            String prompt = buildCodeReviewPrompt(code, context);
            return getAIResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating code review", e);
            throw new RuntimeException("Failed to generate code review", e);
        }
    }

    @Override
    public List<String> generateTestSuggestions(String code) {
        try {
            String prompt = buildTestSuggestionPrompt(code);
            String response = getAIResponse(prompt);
            return Arrays.asList(response.split("\n"));
        } catch (Exception e) {
            log.error("Error generating test suggestions", e);
            throw new RuntimeException("Failed to generate test suggestions", e);
        }
    }

    @Override
    public List<String> generateDocumentationSuggestions(String code) {
        try {
            String prompt = buildDocumentationPrompt(code);
            String response = getAIResponse(prompt);
            return Arrays.asList(response.split("\n"));
        } catch (Exception e) {
            log.error("Error generating documentation suggestions", e);
            throw new RuntimeException("Failed to generate documentation suggestions", e);
        }
    }

    @Override
    public Map<String, String> analyzeCodeQuality(String code) {
        try {
            String prompt = buildCodeQualityPrompt(code);
            String response = getAIResponse(prompt);
            return parseCodeQualityResponse(response);
        } catch (Exception e) {
            log.error("Error analyzing code quality", e);
            throw new RuntimeException("Failed to analyze code quality", e);
        }
    }

    @Override
    public String generateRefactoringSuggestions(String code) {
        try {
            String prompt = buildRefactoringPrompt(code);
            return getAIResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating refactoring suggestions", e);
            throw new RuntimeException("Failed to generate refactoring suggestions", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            if (openAiService == null) {
                initializeService();
            }
            return true;
        } catch (Exception e) {
            log.error("OpenAI service is not available", e);
            return false;
        }
    }

    private void initializeService() {
        LLMConfig.ApiConfig apiConfig = llmConfig.getOpenai().getApi();
        openAiService = new OpenAiService(apiConfig.getKey(), Duration.ofSeconds(apiConfig.getTimeout()));
    }

    private String getAIResponse(String prompt) {
        if (openAiService == null) {
            initializeService();
        }

        LLMConfig.ApiConfig apiConfig = llmConfig.getOpenai().getApi();
        ChatMessage message = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(apiConfig.getModel())
                .messages(Collections.singletonList(message))
                .temperature(apiConfig.getTemperature())
                .maxTokens(apiConfig.getMaxTokens())
                .build();

        return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
    }

    private String buildCodeReviewPrompt(String code, String context) {
        return String.format("""
            Review the following code for logic correctness, security vulnerabilities, performance issues, and best practices.
            Consider the project context: %s
            
            Code:
            %s
            
            Provide a detailed review with specific suggestions for improvement.
            """, context, code);
    }

    private String buildTestSuggestionPrompt(String code) {
        return String.format("""
            Suggest unit tests for the following code:
            
            Code:
            %s
            
            Provide specific test cases that should be implemented.
            """, code);
    }

    private String buildDocumentationPrompt(String code) {
        return String.format("""
            Suggest documentation improvements for the following code:
            
            Code:
            %s
            
            Provide specific documentation suggestions.
            """, code);
    }

    private String buildCodeQualityPrompt(String code) {
        return String.format("""
            Analyze the code quality of the following code:
            
            Code:
            %s
            
            Provide analysis in the following format:
            COMPLEXITY|score
            MAINTAINABILITY|score
            RELIABILITY|score
            SECURITY|score
            PERFORMANCE|score
            """, code);
    }

    private String buildRefactoringPrompt(String code) {
        return String.format("""
            Suggest refactoring improvements for the following code:
            
            Code:
            %s
            
            Provide specific refactoring suggestions with code examples.
            """, code);
    }

    private Map<String, String> parseCodeQualityResponse(String response) {
        Map<String, String> qualityScores = new HashMap<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) {
                qualityScores.put(parts[0].trim(), parts[1].trim());
            }
        }
        
        return qualityScores;
    }
} 