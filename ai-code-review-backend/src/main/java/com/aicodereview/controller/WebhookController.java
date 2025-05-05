package com.aicodereview.controller;

import com.aicodereview.service.CodeReviewService;
import com.aicodereview.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final CodeReviewService codeReviewService;
    private final GitHubService gitHubService;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestHeader("X-GitHub-Event") String event) {

        try {
            // Verify webhook signature
            gitHubService.verifyWebhookSignature(payload, signature);

            // Process only pull request events
            if ("pull_request".equals(event)) {
                // Parse payload and extract PR information
                // This is a simplified version - you'll need to implement proper JSON parsing
                String repositoryName = extractRepositoryName(payload);
                String pullRequestId = extractPullRequestId(payload);
                String commitSha = extractCommitSha(payload);

                // Create and process review
                codeReviewService.createReview(repositoryName, pullRequestId, commitSha);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }

    private String extractRepositoryName(String payload) {
        // Implement JSON parsing to extract repository name
        // This is a placeholder implementation
        return "owner/repo";
    }

    private String extractPullRequestId(String payload) {
        // Implement JSON parsing to extract PR ID
        // This is a placeholder implementation
        return "1";
    }

    private String extractCommitSha(String payload) {
        // Implement JSON parsing to extract commit SHA
        // This is a placeholder implementation
        return "abc123";
    }
} 