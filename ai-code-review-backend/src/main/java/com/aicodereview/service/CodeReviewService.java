package com.aicodereview.service;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewStatus;

public interface CodeReviewService {
    CodeReview createReview(String repositoryName, String pullRequestId, String commitSha);
    CodeReview getReview(Long id);
    CodeReview updateReviewStatus(Long id, ReviewStatus status);
    void processReview(Long id);
} 