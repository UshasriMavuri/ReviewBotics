package com.aicodereview.service;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewComment;
import java.util.List;
import java.util.Map;

public interface GitHubPRService {
    CodeReview fetchPullRequestDetails(String repositoryName, String pullRequestId);
    String getPullRequestDiff(String repositoryName, String pullRequestId);
    void postReviewComment(String repositoryName, String pullRequestId, ReviewComment comment);
    void postReviewSummary(String repositoryName, String pullRequestId, List<ReviewComment> comments);
    Map<String, List<String>> getFileOwnership(String repositoryName);
    List<String> suggestReviewers(String repositoryName, String pullRequestId);
    void updateReviewStatus(String repositoryName, String pullRequestId, String status);
    void requestReviewers(String repositoryName, String pullRequestId, List<String> reviewers);
    void verifyWebhookSignature(String payload, String signature);
    String getPullRequestState(String repositoryName, String pullRequestId);
} 