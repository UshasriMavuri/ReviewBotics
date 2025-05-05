package com.aicodereview.service;

public interface GitHubService {
    String getPullRequestDiff(String repositoryName, String pullRequestId);
    void postComment(String repositoryName, String pullRequestId, String filePath, int lineNumber, String comment);
    void verifyWebhookSignature(String payload, String signature);
} 