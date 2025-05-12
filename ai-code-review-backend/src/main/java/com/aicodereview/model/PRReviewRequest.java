package com.aicodereview.model;

import lombok.Data;

@Data
public class PRReviewRequest {
    private String repositoryName;
    private String pullRequestId;
} 