package com.aicodereview.controller;

import com.aicodereview.model.CodeReview;
import com.aicodereview.service.CodeReviewService;
import com.aicodereview.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CodeReviewController {
    private static final Logger logger = LoggerFactory.getLogger(CodeReviewController.class);

    @Autowired
    private CodeReviewService codeReviewService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/review")
    public ResponseEntity<CodeReview> reviewCode(@RequestBody CodeReviewRequest request) {
        CodeReview review = new CodeReview();
        review.setCode(request.getCode());
        return ResponseEntity.ok(codeReviewService.createReview(review));
    }

    @PostMapping("/pr/review")
    public ResponseEntity<?> reviewPullRequest(@RequestBody PRReviewRequest request) {
        try {
            logger.info("Received PR review request for repository: {} and PR ID: {}", 
                request.getRepositoryName(), request.getPullRequestId());
            
            if (request.getRepositoryName() == null || request.getPullRequestId() == null) {
                logger.error("Missing required parameters: repositoryName or pullRequestId");
                return ResponseEntity.badRequest().body("Missing required parameters");
            }

            String sanitizedRepoName = securityUtil.sanitizeRepositoryName(request.getRepositoryName());
            String sanitizedPRNumber = securityUtil.sanitizePRNumber(request.getPullRequestId());

            CodeReview review = codeReviewService.reviewPullRequest(
                sanitizedRepoName,
                sanitizedPRNumber
            );
            
            logger.info("Successfully completed PR review");
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            logger.error("Error processing PR review request", e);
            return ResponseEntity.internalServerError()
                .body("Error processing request: " + e.getMessage());
        }
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<CodeReview> getReview(@PathVariable Long id) {
        CodeReview review = codeReviewService.getReview(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<CodeReview>> getAllReviews() {
        List<CodeReview> reviews = codeReviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<CodeReview> updateReview(@PathVariable Long id, @RequestBody CodeReview review) {
        CodeReview updatedReview = codeReviewService.updateReview(id, review);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        codeReviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}

class CodeReviewRequest {
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

class PRReviewRequest {
    private String repositoryName;
    private String pullRequestId;

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }
} 