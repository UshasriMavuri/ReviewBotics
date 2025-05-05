package com.aicodereview.service.impl;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewComment;
import com.aicodereview.model.CommentType;
import com.aicodereview.service.AIReviewService;
import com.aicodereview.service.GitHubService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewServiceImpl implements AIReviewService {

    private final GitHubService gitHubService;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.temperature}")
    private Double temperature;

    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    @Override
    public void analyzeCode(String diff, CodeReview review) {
        OpenAiService service = new OpenAiService(openaiApiKey, Duration.ofSeconds(60));

        String prompt = buildPrompt(diff);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are an expert code reviewer. Analyze the code and provide specific, actionable feedback."));
        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        try {
            String response = service.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();

            processAIResponse(response, review);
        } catch (Exception e) {
            log.error("Error analyzing code with AI", e);
            throw new RuntimeException("Failed to analyze code", e);
        }
    }

    private String buildPrompt(String diff) {
        return String.format("""
            Please review the following code changes and provide feedback on:
            1. Logic correctness
            2. Security vulnerabilities
            3. Performance bottlenecks
            4. Code style and best practices
            5. Missing tests
            6. Documentation needs
            7. Refactoring opportunities

            For each issue found, provide:
            - File path
            - Line number
            - Issue type
            - Detailed explanation
            - Suggested fix (if applicable)

            Code changes:
            %s
            """, diff);
    }

    private void processAIResponse(String response, CodeReview review) {
        // Parse the AI response and create review comments
        // This is a simplified version - you'll need to implement proper parsing
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.contains("File:") && line.contains("Line:")) {
                String[] parts = line.split(":");
                String filePath = parts[1].trim();
                int lineNumber = Integer.parseInt(parts[2].trim());
                String comment = line.substring(line.indexOf(":", line.indexOf("Line:")) + 1).trim();

                ReviewComment reviewComment = new ReviewComment();
                reviewComment.setCodeReview(review);
                reviewComment.setFilePath(filePath);
                reviewComment.setLineNumber(lineNumber);
                reviewComment.setComment(comment);
                reviewComment.setType(determineCommentType(comment));

                review.getComments().add(reviewComment);

                // Post comment to GitHub
                gitHubService.postComment(
                    review.getRepositoryName(),
                    review.getPullRequestId(),
                    filePath,
                    lineNumber,
                    comment
                );
            }
        }
    }

    private CommentType determineCommentType(String comment) {
        String lowerComment = comment.toLowerCase();
        if (lowerComment.contains("security") || lowerComment.contains("vulnerability")) {
            return CommentType.SECURITY;
        } else if (lowerComment.contains("performance") || lowerComment.contains("bottleneck")) {
            return CommentType.PERFORMANCE;
        } else if (lowerComment.contains("test") || lowerComment.contains("coverage")) {
            return CommentType.TEST_COVERAGE;
        } else if (lowerComment.contains("document") || lowerComment.contains("comment")) {
            return CommentType.DOCUMENTATION;
        } else if (lowerComment.contains("refactor") || lowerComment.contains("improve")) {
            return CommentType.REFACTORING;
        } else if (lowerComment.contains("style") || lowerComment.contains("convention")) {
            return CommentType.STYLE;
        } else {
            return CommentType.LOGIC;
        }
    }
} 