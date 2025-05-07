package com.aicodereview.service.impl;

import com.aicodereview.config.OllamaConfig;
import com.aicodereview.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class OllamaService implements LLMService {

    private final OllamaConfig ollamaConfig;
    private final RestTemplate restTemplate;

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
            String healthCheckUrl = ollamaConfig.getApi().getUrl() + "/api/tags";
            restTemplate.getForObject(healthCheckUrl, String.class);
            return true;
        } catch (Exception e) {
            log.error("Ollama service is not available", e);
            return false;
        }
    }

    private String getAIResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaConfig.getApi().getModel());
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("temperature", ollamaConfig.getApi().getTemperature());
        requestBody.put("max_tokens", ollamaConfig.getApi().getMaxTokens());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        String generateUrl = ollamaConfig.getApi().getUrl() + "/api/generate";
        Map<String, Object> response = restTemplate.postForObject(
            generateUrl,
            request,
            Map.class
        );

        return response != null ? response.get("response").toString() : "";
    }

    private String buildCodeReviewPrompt(String code, String context) {
        return String.format("""
            You are an expert code reviewer. Review the following code for logic correctness, security vulnerabilities, performance issues, and best practices.
            Consider the project context: %s
            
            Code:
            %s
            
            Provide a detailed review with specific suggestions for improvement. Format your response as:
            1. Critical Issues (if any)
            2. Security Concerns
            3. Performance Considerations
            4. Code Style and Best Practices
            5. Suggested Improvements
            """, context, code);
    }

    private String buildTestSuggestionPrompt(String code) {
        return String.format("""
            You are a testing expert. Suggest unit tests for the following code:
            
            Code:
            %s
            
            Provide specific test cases that should be implemented. For each test case, include:
            1. Test name
            2. Test scenario
            3. Expected input
            4. Expected output
            """, code);
    }

    private String buildDocumentationPrompt(String code) {
        return String.format("""
            You are a documentation expert. Suggest documentation improvements for the following code:
            
            Code:
            %s
            
            Provide specific documentation suggestions including:
            1. Function/method documentation
            2. Class documentation
            3. Important notes and warnings
            4. Usage examples
            """, code);
    }

    private String buildCodeQualityPrompt(String code) {
        return String.format("""
            Analyze the code quality of the following code:
            
            Code:
            %s
            
            Provide analysis in the following format:
            COMPLEXITY|score (1-10)
            MAINTAINABILITY|score (1-10)
            RELIABILITY|score (1-10)
            SECURITY|score (1-10)
            PERFORMANCE|score (1-10)
            
            For each metric, provide a brief explanation of the score.
            """, code);
    }

    private String buildRefactoringPrompt(String code) {
        return String.format("""
            You are a refactoring expert. Suggest refactoring improvements for the following code:
            
            Code:
            %s
            
            Provide specific refactoring suggestions with:
            1. Current issue
            2. Suggested improvement
            3. Code example of the improvement
            4. Benefits of the change
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