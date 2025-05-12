package com.aicodereview.service.impl;

import com.aicodereview.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements LLMService {

    private final RestTemplate restTemplate;

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.model:codellama}")
    private String model;

    @Override
    public String generateCodeReview(String diff, String context) {
        try {
            String prompt = String.format(
                       "You are an expert code reviewer for a Spring Boot and React project.\n\n" +
                        "Code Context:\n%s\n\n" +
                        "Code Changes:\n%s\n\n" +
                        "Instructions:\n" +
                        "- Only add comments if something is missing, incorrect, insecure, or poorly written.\n" +
                        "- Do not comment if the code is clean and follows best practices.\n" +
                        "- Format each comment as a single bullet point.\n" +
                        "- Each comment should start with the filename (if inferable), followed by a colon, then a short, specific suggestion.\n" +
                        "- Keep each comment to one sentence. Do not include long explanations or examples.\n" +
                        "- If no issues found, respond with: No comments needed.",
                        context, diff
            );

            return generateResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating code review with Ollama", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String analyzeCodeQuality(String code) {
        try {
            String prompt = String.format(
                "You are an expert code reviewer. Analyze the following code for quality issues:\n\n" +
                "Code:\n%s\n\n" +
                "Please provide a detailed analysis focusing on:\n" +
                "1. Code complexity\n" +
                "2. Maintainability\n" +
                "3. Readability\n" +
                "4. Best practices\n" +
                "5. Potential improvements\n\n" +
                "Format your response in a clear, structured way.",
                code
            );

            return generateResponse(prompt);
        } catch (Exception e) {
            log.error("Error analyzing code quality with Ollama", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String generateRefactoringSuggestions(String code) {
        try {
            String prompt = String.format(
                "You are an expert code reviewer. Analyze the following code and suggest refactoring improvements:\n\n" +
                "Code:\n%s\n\n" +
                "Please provide specific refactoring suggestions focusing on:\n" +
                "1. Code structure\n" +
                "2. Design patterns\n" +
                "3. Code duplication\n" +
                "4. Naming conventions\n" +
                "5. Specific refactoring steps\n\n" +
                "Format your response in a clear, structured way.",
                code
            );

            return generateResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating refactoring suggestions with Ollama", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String generateTestSuggestions(String code) {
        try {
            String prompt = String.format(
                "You are an expert code reviewer. Analyze the following code and suggest test cases:\n\n" +
                "Code:\n%s\n\n" +
                "Please provide test suggestions focusing on:\n" +
                "1. Unit tests\n" +
                "2. Edge cases\n" +
                "3. Integration tests\n" +
                "4. Test coverage\n" +
                "5. Specific test scenarios\n\n" +
                "Format your response in a clear, structured way.",
                code
            );

            return generateResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating test suggestions with Ollama", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String generateDocumentationSuggestions(String code) {
        try {
            String prompt = String.format(
                "You are an expert code reviewer. Analyze the following code and suggest documentation improvements:\n\n" +
                "Code:\n%s\n\n" +
                "Please provide documentation suggestions focusing on:\n" +
                "1. Code comments\n" +
                "2. API documentation\n" +
                "3. README updates\n" +
                "4. Usage examples\n" +
                "5. Specific documentation needs\n\n" +
                "Format your response in a clear, structured way.",
                code
            );

            return generateResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating documentation suggestions with Ollama", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            String url = ollamaApiUrl + "/api/tags";
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.error("Ollama service is not available", e);
            return false;
        }
    }

    private String generateResponse(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        String url = ollamaApiUrl + "/api/generate";
        log.debug("Making request to Ollama API: {}", url);

        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        
        if (response != null && response.containsKey("response")) {
            return (String) response.get("response");
        } else {
            log.error("Invalid response from Ollama API: {}", response);
            return "Error: Unable to generate response";
        }
    }
} 