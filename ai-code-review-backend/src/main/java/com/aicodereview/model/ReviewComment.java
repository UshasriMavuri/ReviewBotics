package com.aicodereview.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComment {
    private Long id;
    private CodeReview review;
    private String filePath;
    private Integer lineNumber;
    private CommentType type;
    private String severity;
    private String category;
    private String comment;
    private String suggestedFix;
    private LocalDateTime createdAt;
    private String context;
    private Boolean resolved;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
} 