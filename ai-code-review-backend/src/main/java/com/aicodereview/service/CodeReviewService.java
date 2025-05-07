package com.aicodereview.service;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewStatus;

import java.util.List;

public interface CodeReviewService {
    CodeReview createReview(CodeReview review);
    CodeReview getReview(Long id);
    List<CodeReview> getAllReviews();
    CodeReview updateReview(Long id, CodeReview review);
    void deleteReview(Long id);
    CodeReview reviewPullRequest(String repositoryName, String pullRequestId);
    CodeReview updateReviewStatus(Long id, ReviewStatus status);
    void processReview(Long id);
} 