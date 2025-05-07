package com.aicodereview.controller;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewComment;
import com.aicodereview.service.AIReviewService;
import com.aicodereview.service.CodeReviewService;
import com.aicodereview.service.GitHubPRService;
import com.aicodereview.service.MCPService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final CodeReviewService codeReviewService;
    private final GitHubPRService gitHubPRService;
    private final AIReviewService aiReviewService;
    private final MCPService mcpService;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestHeader("X-GitHub-Event") String event) {

        try {
            // Verify webhook signature
            gitHubPRService.verifyWebhookSignature(payload, signature);

            // Process only pull request events
            if ("pull_request".equals(event)) {
                JsonNode jsonNode = objectMapper.readTree(payload);
                String action = jsonNode.get("action").asText();

                // Process only opened, synchronize, and reopened events
                if ("opened".equals(action) || "synchronize".equals(action) || "reopened".equals(action)) {
                    String repositoryName = jsonNode.get("repository").get("full_name").asText();
                    String pullRequestId = jsonNode.get("pull_request").get("number").asText();
                    String commitSha = jsonNode.get("pull_request").get("head").get("sha").asText();

                    // Get project context from MCP
                    String mcpContext = mcpService.getProjectContext(repositoryName);

                    // Create review
                    CodeReview review = new CodeReview();
                    review.setRepositoryName(repositoryName);
                    review.setPullRequestId(pullRequestId);
                    review.setCommitSha(commitSha);
                    review = codeReviewService.createReview(review);

                    // Get AI review comments
                    List<ReviewComment> comments = aiReviewService.reviewPullRequest(repositoryName, pullRequestId, mcpContext);

                    // Post comments to GitHub
                    for (ReviewComment comment : comments) {
                        gitHubPRService.postReviewComment(repositoryName, pullRequestId, comment);
                    }

                    // Post review summary
                    gitHubPRService.postReviewSummary(repositoryName, pullRequestId, comments);

                    // Suggest reviewers
                    List<String> suggestedReviewers = gitHubPRService.suggestReviewers(repositoryName, pullRequestId);
                    if (!suggestedReviewers.isEmpty()) {
                        gitHubPRService.requestReviewers(repositoryName, pullRequestId, suggestedReviewers);
                    }

                    // Update review status
                    gitHubPRService.updateReviewStatus(repositoryName, pullRequestId, "success");
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }
} 