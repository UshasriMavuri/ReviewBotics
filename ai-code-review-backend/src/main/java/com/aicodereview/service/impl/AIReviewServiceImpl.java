package com.aicodereview.service.impl;

import com.aicodereview.model.ReviewComment;
import com.aicodereview.model.CommentType;
import com.aicodereview.service.AIReviewService;
import com.aicodereview.service.GitHubPRService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewServiceImpl implements AIReviewService {

    private final GitHubPRService gitHubPRService;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.model}")
    private String openaiModel;

    @Value("${openai.api.temperature}")
    private Double temperature;

    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    private OpenAiService openAiService;

    @Override
    public List<ReviewComment> reviewCode(String code, String filePath, String mcpContext) {
        try {
            String prompt = buildReviewPrompt(code, filePath, mcpContext);
            String response = getAIResponse(prompt);
            return parseReviewResponse(response, filePath);
        } catch (Exception e) {
            log.error("Error reviewing code", e);
            throw new RuntimeException("Failed to review code", e);
        }
    }

    @Override
    public List<ReviewComment> reviewPullRequest(String repositoryName, String pullRequestId, String mcpContext) {
        try {
            String diff = gitHubPRService.getPullRequestDiff(repositoryName, pullRequestId);
            String prompt = buildPRReviewPrompt(diff, mcpContext);
            String response = getAIResponse(prompt);
            return parseReviewResponse(response, null);
        } catch (Exception e) {
            log.error("Error reviewing pull request", e);
            throw new RuntimeException("Failed to review pull request", e);
        }
    }

    @Override
    public String generateReviewSummary(List<ReviewComment> comments) {
        try {
            String prompt = buildSummaryPrompt(comments);
            return getAIResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating review summary", e);
            throw new RuntimeException("Failed to generate review summary", e);
        }
    }

    @Override
    public List<String> suggestTests(String code, String filePath) {
        try {
            String prompt = buildTestSuggestionPrompt(code, filePath);
            String response = getAIResponse(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting tests", e);
            throw new RuntimeException("Failed to suggest tests", e);
        }
    }

    @Override
    public List<String> suggestDocumentation(String code, String filePath) {
        try {
            String prompt = buildDocumentationPrompt(code, filePath);
            String response = getAIResponse(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting documentation", e);
            throw new RuntimeException("Failed to suggest documentation", e);
        }
    }

    @Override
    public List<String> suggestRefactoring(String code, String filePath) {
        try {
            String prompt = buildRefactoringPrompt(code, filePath);
            String response = getAIResponse(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting refactoring", e);
            throw new RuntimeException("Failed to suggest refactoring", e);
        }
    }

    @Override
    public List<String> generateDocumentationSuggestions(String code) {
        try {
            String prompt = buildDocumentationPrompt(code, "file");
            String response = getAIResponse(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error generating documentation suggestions", e);
            throw new RuntimeException("Failed to generate documentation suggestions", e);
        }
    }

    @Override
    public List<String> generateTestSuggestions(String code) {
        try {
            String prompt = buildTestSuggestionPrompt(code, "file");
            String response = getAIResponse(prompt);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error generating test suggestions", e);
            throw new RuntimeException("Failed to generate test suggestions", e);
        }
    }

    @Override
    public String generateRefactoringSuggestions(String code) {
        try {
            String prompt = buildRefactoringPrompt(code, "file");
            return getAIResponse(prompt);
        } catch (Exception e) {
            log.error("Error generating refactoring suggestions", e);
            throw new RuntimeException("Failed to generate refactoring suggestions", e);
        }
    }

    @Override
    public Map<String, String> analyzeCodeQuality(String code) {
        try {
            String prompt = String.format("""
                Analyze the code quality of the following code:
                
                %s
                
                Provide analysis in the format:
                METRIC|SCORE|DESCRIPTION
                """, code);
            String response = getAIResponse(prompt);
            return parseQualityAnalysis(response);
        } catch (Exception e) {
            log.error("Error analyzing code quality", e);
            throw new RuntimeException("Failed to analyze code quality", e);
        }
    }

    private String buildReviewPrompt(String code, String filePath, String mcpContext) {
        return String.format("""
            Review the following code for logic correctness, security vulnerabilities, performance issues, and best practices.
            Consider the project context: %s
            
            File: %s
            Code:
            %s
            
            Provide a detailed review with specific line numbers and suggestions for improvement.
            Format each comment as:
            LINE_NUMBER|TYPE|SEVERITY|CATEGORY|COMMENT|SUGGESTED_FIX
            """, mcpContext, filePath, code);
    }

    private String buildPRReviewPrompt(String diff, String mcpContext) {
        return String.format("""
            Review the following pull request diff for logic correctness, security vulnerabilities, performance issues, and best practices.
            Consider the project context: %s
            
            Diff:
            %s
            
            Provide a detailed review with specific line numbers and suggestions for improvement.
            Format each comment as:
            FILE_PATH|LINE_NUMBER|TYPE|SEVERITY|CATEGORY|COMMENT|SUGGESTED_FIX
            """, mcpContext, diff);
    }

    private String buildSummaryPrompt(List<ReviewComment> comments) {
        StringBuilder prompt = new StringBuilder("Generate a summary of the following code review comments:\n\n");
        for (ReviewComment comment : comments) {
            prompt.append(String.format("- %s: %s\n", comment.getType(), comment.getComment()));
        }
        prompt.append("\nProvide a concise summary highlighting the key issues and recommendations.");
        return prompt.toString();
    }

    private String buildTestSuggestionPrompt(String code, String filePath) {
        return String.format("""
            Suggest unit tests for the following code:
            
            File: %s
            Code:
            %s
            
            Provide specific test cases that should be implemented.
            Format each suggestion as a separate line.
            """, filePath, code);
    }

    private String buildDocumentationPrompt(String code, String filePath) {
        return String.format("""
            Suggest documentation improvements for the following code:
            
            File: %s
            Code:
            %s
            
            Provide specific documentation suggestions.
            Format each suggestion as a separate line.
            """, filePath, code);
    }

    private String buildRefactoringPrompt(String code, String filePath) {
        return String.format("""
            Suggest refactoring improvements for the following code:
            
            File: %s
            Code:
            %s
            
            Provide specific refactoring suggestions with code examples.
            Format each suggestion as a separate line.
            """, filePath, code);
    }

    private String getAIResponse(String prompt) {
        if (openAiService == null) {
            openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(30));
        }

        ChatMessage message = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(openaiModel)
                .messages(Collections.singletonList(message))
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
    }

    private List<ReviewComment> parseReviewResponse(String response, String defaultFilePath) {
        List<ReviewComment> comments = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length < 5) continue;

            ReviewComment comment = new ReviewComment();
            comment.setFilePath(parts.length > 5 ? parts[0] : defaultFilePath);
            comment.setLineNumber(Integer.parseInt(parts.length > 5 ? parts[1] : parts[0]));
            comment.setType(CommentType.valueOf(parts.length > 5 ? parts[2] : parts[1]));
            comment.setSeverity(parts.length > 5 ? parts[3] : parts[2]);
            comment.setCategory(parts.length > 5 ? parts[4] : parts[3]);
            comment.setComment(parts.length > 5 ? parts[5] : parts[4]);
            if (parts.length > 6) {
                comment.setSuggestedFix(parts[6]);
            }

            comments.add(comment);
        }

        return comments;
    }

    private List<String> parseListResponse(String response) {
        return Arrays.stream(response.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private Map<String, String> parseQualityAnalysis(String response) {
        Map<String, String> analysis = new HashMap<>();
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                analysis.put(parts[0], parts[1] + "|" + parts[2]);
            }
        }
        return analysis;
    }
} 