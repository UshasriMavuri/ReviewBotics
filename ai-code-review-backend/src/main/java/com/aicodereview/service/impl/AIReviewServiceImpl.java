package com.aicodereview.service.impl;

import com.aicodereview.model.ReviewComment;
import com.aicodereview.model.CommentType;
import com.aicodereview.service.AIReviewService;
import com.aicodereview.service.GitHubPRService;
import com.aicodereview.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewServiceImpl implements AIReviewService {

    private final GitHubPRService gitHubPRService;
    private final LLMService llmService;

    @Override
    public List<ReviewComment> reviewCode(String code, String filePath, String mcpContext) {
        try {
            String prompt = buildReviewPrompt(code, filePath, mcpContext);
            String response = llmService.generateCodeReview(code, mcpContext);
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
            String response = llmService.generateCodeReview(diff, mcpContext);
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
            return llmService.generateCodeReview(prompt, "Summary generation");
        } catch (Exception e) {
            log.error("Error generating review summary", e);
            throw new RuntimeException("Failed to generate review summary", e);
        }
    }

    @Override
    public List<String> suggestTests(String code, String filePath) {
        try {
            String response = llmService.generateTestSuggestions(code);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting tests", e);
            throw new RuntimeException("Failed to suggest tests", e);
        }
    }

    @Override
    public List<String> suggestDocumentation(String code, String filePath) {
        try {
            String response = llmService.generateDocumentationSuggestions(code);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting documentation", e);
            throw new RuntimeException("Failed to suggest documentation", e);
        }
    }

    @Override
    public List<String> suggestRefactoring(String code, String filePath) {
        try {
            String response = llmService.generateRefactoringSuggestions(code);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error suggesting refactoring", e);
            throw new RuntimeException("Failed to suggest refactoring", e);
        }
    }

    @Override
    public List<String> generateDocumentationSuggestions(String code) {
        try {
            String response = llmService.generateDocumentationSuggestions(code);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error generating documentation suggestions", e);
            throw new RuntimeException("Failed to generate documentation suggestions", e);
        }
    }

    @Override
    public List<String> generateTestSuggestions(String code) {
        try {
            String response = llmService.generateTestSuggestions(code);
            return parseListResponse(response);
        } catch (Exception e) {
            log.error("Error generating test suggestions", e);
            throw new RuntimeException("Failed to generate test suggestions", e);
        }
    }

    @Override
    public String generateRefactoringSuggestions(String code) {
        try {
            return llmService.generateRefactoringSuggestions(code);
        } catch (Exception e) {
            log.error("Error generating refactoring suggestions", e);
            throw new RuntimeException("Failed to generate refactoring suggestions", e);
        }
    }

    @Override
    public Map<String, String> analyzeCodeQuality(String code) {
        try {
            String response = llmService.analyzeCodeQuality(code);
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
            
            Provide a concise review focusing on critical issues only. Format each comment as:
            FILENAME: ISSUE_DESCRIPTION
            
            For example:
            UserService.java: Missing null check for user input.
            LoginController.java: Avoid logging sensitive info like passwords.
            UserServiceTest.java: No tests added for deleteUser method.
            
            Only include comments for actual issues found. If no issues are found, return an empty list.
            """, mcpContext, filePath, code);
    }

    private String buildPRReviewPrompt(String diff, String mcpContext) {
        return String.format("""
            Review the following pull request diff for logic correctness, security vulnerabilities, performance issues, and best practices.
            Consider the project context: %s
            
            Diff:
            %s
            
            Provide a concise review focusing on critical issues only. Format each comment as:
            FILENAME: ISSUE_DESCRIPTION
            
            For example:
            UserService.java: Missing null check for user input.
            LoginController.java: Avoid logging sensitive info like passwords.
            UserServiceTest.java: No tests added for deleteUser method.
            
            Only include comments for actual issues found. If no issues are found, return an empty list.
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

    private List<ReviewComment> parseReviewResponse(String response, String defaultFilePath) {
        List<ReviewComment> comments = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("For example:") || line.startsWith("Only include")) {
                continue;
            }

            // Parse lines in the format "FILENAME: ISSUE_DESCRIPTION"
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String filePath = line.substring(0, colonIndex).trim();
                String comment = line.substring(colonIndex + 1).trim();

                ReviewComment reviewComment = new ReviewComment();
                reviewComment.setFilePath(filePath);
                reviewComment.setComment(comment);
                reviewComment.setType(CommentType.CODE_SMELL);
                reviewComment.setSeverity("MEDIUM");
                reviewComment.setCategory("CODE_QUALITY");

                comments.add(reviewComment);
            }
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