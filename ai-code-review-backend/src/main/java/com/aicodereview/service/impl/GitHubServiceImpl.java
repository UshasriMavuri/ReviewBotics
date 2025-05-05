package com.aicodereview.service.impl;

import com.aicodereview.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {

    @Value("${github.webhook.secret}")
    private String webhookSecret;

    @Value("${github.app.id}")
    private String appId;

    @Value("${github.app.private-key}")
    private String privateKey;

    private GitHub gitHub;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String getPullRequestDiff(String repositoryName, String pullRequestId) {
        try {
            GitHub gitHub = getGitHubInstance();
            String diffUrl = String.format("https://api.github.com/repos/%s/pulls/%s", repositoryName, pullRequestId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(diffUrl))
                    .header("Accept", "application/vnd.github.v3.diff")
                    .header("Authorization", "token " + System.getenv("GITHUB_TOKEN"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            log.error("Error getting PR diff", e);
            throw new RuntimeException("Failed to get PR diff", e);
        }
    }

    @Override
    public void postComment(String repositoryName, String pullRequestId, String filePath, int lineNumber, String comment) {
        try {
            GitHub gitHub = getGitHubInstance();
            GHRepository repository = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repository.getPullRequest(Integer.parseInt(pullRequestId));
            
            pullRequest.createReviewComment(comment, pullRequest.getHead().getSha(), filePath, lineNumber);
        } catch (Exception e) {
            log.error("Error posting comment", e);
            throw new RuntimeException("Failed to post comment", e);
        }
    }

    @Override
    public void verifyWebhookSignature(String payload, String signature) {
        try {
            String expectedSignature = "sha1=" + calculateHmac(payload, webhookSecret);
            if (!expectedSignature.equals(signature)) {
                throw new SecurityException("Invalid webhook signature");
            }
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            throw new SecurityException("Failed to verify webhook signature", e);
        }
    }

    private GitHub getGitHubInstance() throws Exception {
        if (gitHub == null) {
            // Initialize GitHub instance with app authentication
            // This is a simplified version - you'll need to implement proper JWT token generation
            gitHub = GitHub.connectUsingOAuth(System.getenv("GITHUB_TOKEN"));
        }
        return gitHub;
    }

    private String calculateHmac(String payload, String secret) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }
} 