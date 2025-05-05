package com.aicodereview.service;

import com.aicodereview.model.CodeReview;

public interface AIReviewService {
    void analyzeCode(String diff, CodeReview review);
} 