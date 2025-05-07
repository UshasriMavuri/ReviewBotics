package com.aicodereview.service.impl;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewComment;
import com.aicodereview.model.CommentType;
import com.aicodereview.service.GitHubPRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.time.Instant;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubPRServiceImpl implements GitHubPRService {

    @Value("${github.app.id}")
    private String appId;

    @Value("${github.app.private-key}")
    private String privateKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String installationToken;

    private String generateJWT() throws Exception {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(600); // 10 minutes

        String header = Base64.getEncoder().encodeToString(
            objectMapper.writeValueAsString(Map.of(
                "alg", "RS256",
                "typ", "JWT"
            )).getBytes()
        );

        String payload = Base64.getEncoder().encodeToString(
            objectMapper.writeValueAsString(Map.of(
                "iat", now.getEpochSecond(),
                "exp", expiration.getEpochSecond(),
                "iss", appId
            )).getBytes()
        );

        String privateKeyPEM = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update((header + "." + payload).getBytes());

        String signatureBase64 = Base64.getEncoder().encodeToString(signature.sign());
        return header + "." + payload + "." + signatureBase64;
    }

    private String getInstallationToken(String repositoryName) throws Exception {
        String[] parts = repositoryName.split("/");
        String owner = parts[0];

        // Get installation ID
        String jwt = generateJWT();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/app/installations"))
                .header("Authorization", "Bearer " + jwt)
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        List<Map<String, Object>> installations = objectMapper.readValue(response.body(), List.class);
        
        String installationId = null;
        for (Map<String, Object> installation : installations) {
            Map<String, Object> account = (Map<String, Object>) installation.get("account");
            if (owner.equals(account.get("login"))) {
                installationId = installation.get("id").toString();
                break;
            }
        }

        if (installationId == null) {
            throw new RuntimeException("GitHub App is not installed on this repository. Please install the app first.");
        }

        // Get installation token
        request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/app/installations/" + installationId + "/access_tokens"))
                .header("Authorization", "Bearer " + jwt)
                .header("Accept", "application/vnd.github.v3+json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), Map.class);
        return (String) tokenResponse.get("token");
    }

    @Override
    public CodeReview fetchPullRequestDetails(String repositoryName, String pullRequestId) {
        try {
            String token = getInstallationToken(repositoryName);
            String url = String.format("https://api.github.com/repos/%s/pulls/%s", repositoryName, pullRequestId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                throw new RuntimeException("Repository or PR not found. Make sure the repository exists and the app has access.");
            }
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch PR details: " + response.body());
            }

            Map<String, Object> prData = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> head = (Map<String, Object>) prData.get("head");
            Map<String, Object> base = (Map<String, Object>) prData.get("base");
            Map<String, Object> user = (Map<String, Object>) prData.get("user");
            
            CodeReview review = new CodeReview();
            review.setRepositoryName(repositoryName);
            review.setPullRequestId(pullRequestId);
            review.setCommitSha((String) head.get("sha"));
            review.setBaseBranch((String) base.get("ref"));
            review.setHeadBranch((String) head.get("ref"));
            review.setAuthor((String) user.get("login"));
            review.setTitle((String) prData.get("title"));
            review.setDescription((String) prData.get("body"));

            return review;
        } catch (Exception e) {
            log.error("Error fetching PR details", e);
            throw new RuntimeException("Failed to fetch PR details: " + e.getMessage());
        }
    }

    @Override
    public String getPullRequestDiff(String repositoryName, String pullRequestId) {
        try {
            String token = getInstallationToken(repositoryName);
            String url = String.format("https://api.github.com/repos/%s/pulls/%s", repositoryName, pullRequestId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3.diff")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                throw new RuntimeException("Repository or PR not found. Make sure the repository exists and the app has access.");
            }
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get PR diff: " + response.body());
            }

            return response.body();
        } catch (Exception e) {
            log.error("Error getting PR diff", e);
            throw new RuntimeException("Failed to get PR diff: " + e.getMessage());
        }
    }

    @Override
    public List<String> suggestReviewers(String repositoryName, String pullRequestId) {
        // For now, return an empty list since we can't access private repository data
        return Collections.emptyList();
    }

    @Override
    public void postReviewComment(String repositoryName, String pullRequestId, ReviewComment comment) {
        // This would require authentication
        log.warn("Posting review comments requires repository access. This feature is disabled for public access.");
    }

    @Override
    public void postReviewSummary(String repositoryName, String pullRequestId, List<ReviewComment> comments) {
        // This would require authentication
        log.warn("Posting review summaries requires repository access. This feature is disabled for public access.");
    }

    @Override
    public void updateReviewStatus(String repositoryName, String pullRequestId, String status) {
        // This would require authentication
        log.warn("Updating review status requires repository access. This feature is disabled for public access.");
    }

    @Override
    public Map<String, List<String>> getFileOwnership(String repositoryName) {
        // This would require authentication
        log.warn("Getting file ownership requires repository access. This feature is disabled for public access.");
        return Collections.emptyMap();
    }

    @Override
    public void verifyWebhookSignature(String payload, String signature) {
        // This would require authentication
        log.warn("Verifying webhook signature requires repository access. This feature is disabled for public access.");
    }

    @Override
    public void requestReviewers(String repositoryName, String pullRequestId, List<String> reviewers) {
        // This would require authentication
        log.warn("Requesting reviewers requires repository access. This feature is disabled for public access.");
    }
} 