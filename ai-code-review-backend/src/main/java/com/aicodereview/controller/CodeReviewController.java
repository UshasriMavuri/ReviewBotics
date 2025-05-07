package com.aicodereview.controller;

import com.aicodereview.model.CodeReview;
import com.aicodereview.service.CodeReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CodeReviewController {

    @Autowired
    private CodeReviewService codeReviewService;

    @PostMapping("/review")
    public ResponseEntity<CodeReview> reviewCode(@RequestBody CodeReviewRequest request) {
        CodeReview review = new CodeReview();
        review.setCode(request.getCode());
        return ResponseEntity.ok(codeReviewService.createReview(review));
    }

    @PostMapping("/pr/review")
    public ResponseEntity<CodeReview> reviewPullRequest(@RequestBody PRReviewRequest request) {
        return ResponseEntity.ok(codeReviewService.reviewPullRequest(
            request.getRepositoryName(),
            request.getPullRequestId()
        ));
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