package com.aicodereview.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReview {
    private Long id;
    private String repositoryName;
    private String pullRequestId;
    private String title;
    private String description;
    private String author;
    private ReviewStatus status;
    private List<ReviewComment> comments;
    private String documentationSuggestions;
    private String testSuggestions;
    private String refactoringSuggestions;
    private String qualityAnalysis;
    private String suggestedReviewers;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String code;
    private String baseBranch;
    private String headBranch;
    private String mcpContext;
    private String commitSha;
} 