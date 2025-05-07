package com.aicodereview.service;

import com.aicodereview.model.ReviewComment;
import java.util.List;
import java.util.Map;

public interface AIReviewService {
    List<ReviewComment> reviewCode(String code, String filePath, String mcpContext);
    List<ReviewComment> reviewPullRequest(String repositoryName, String pullRequestId, String context);
    String generateReviewSummary(List<ReviewComment> comments);
    List<String> suggestTests(String code, String filePath);
    List<String> suggestDocumentation(String code, String filePath);
    List<String> suggestRefactoring(String code, String filePath);
    List<String> generateDocumentationSuggestions(String code);
    List<String> generateTestSuggestions(String code);
    String generateRefactoringSuggestions(String code);
    Map<String, String> analyzeCodeQuality(String code);
} 