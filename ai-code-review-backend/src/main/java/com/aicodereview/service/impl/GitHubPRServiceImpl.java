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
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.aicodereview.model.ReviewStatus;

@Slf4j
@Service
public class GitHubPRServiceImpl implements GitHubPRService {

    @Value("${github.app.id}")
    private String appId;

    @Value("${github.app.private-key}")
    private String privateKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private String installationToken;
    private final RestTemplate restTemplate;

    @Autowired
    public GitHubPRServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, Environment environment) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment;
        
        // Initialize BouncyCastle provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.info("BouncyCastle provider initialized");
        }
    }

    private String generateJWT() {
        try {
            log.info("Generating JWT for GitHub App authentication");
            String appId = environment.getProperty("github.app.id");
            String privateKey = environment.getProperty("github.app.private-key");

            if (appId == null || appId.isEmpty() || privateKey == null || privateKey.isEmpty()) {
                throw new IllegalArgumentException("GitHub App ID or private key is not configured");
            }

            log.info("Original private key length: {}", privateKey.length());
            log.info("Original private key format check - contains BEGIN marker: {}", privateKey.contains("-----BEGIN PRIVATE KEY-----"));
            log.info("Original private key format check - contains END marker: {}", privateKey.contains("-----END PRIVATE KEY-----"));

            // Clean the private key by removing the PEM markers and whitespace
            String cleanedKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            log.info("Cleaned private key length: {}", cleanedKey.length());
            log.info("Cleaned private key first 50 chars: {}", cleanedKey.substring(0, Math.min(50, cleanedKey.length())));

            // Decode the Base64 private key
            byte[] decodedKey = Base64.getDecoder().decode(cleanedKey);
            log.info("Decoded key length: {} bytes", decodedKey.length);

            // Create PKCS8EncodedKeySpec
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            log.info("Created PKCS8EncodedKeySpec");

            // Get RSA KeyFactory with BouncyCastle provider
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
            log.info("Created RSA KeyFactory with BouncyCastle provider");

            // Generate private key
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            log.info("Generated private key");

            // Create JWT using only the private key
            Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) key);
            Instant now = Instant.now();
            String jwt = JWT.create()
                .withIssuer(appId)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(600))) // 10 minutes
                .withClaim("iat", now.getEpochSecond())
                .withClaim("exp", now.plusSeconds(600).getEpochSecond())
                .sign(algorithm);

            log.info("Successfully generated JWT");
            return jwt;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            log.error("Invalid key specification: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Failed to generate JWT: " + e.getMessage(), e);
        } catch (JWTCreationException e) {
            log.error("JWT creation error: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Failed to generate JWT: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating JWT: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Failed to generate JWT: " + e.getMessage(), e);
        }
    }

    private String getInstallationToken(String repositoryName) {
        try {
            log.info("Getting installation token for repository: {}", repositoryName);
            
            // Generate JWT for GitHub App authentication
            String jwt = generateJWT();
            if (jwt == null) {
                log.error("Failed to generate JWT for GitHub App authentication");
                return null;
            }
            
            // Get installation ID
            String[] parts = repositoryName.split("/");
            if (parts.length != 2) {
                log.error("Invalid repository name format: {}", repositoryName);
                return null;
            }
            
            String owner = parts[0];
            String url = String.format("https://api.github.com/repos/%s/installation", repositoryName);
            log.debug("Making request to get installation ID: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", environment.getProperty("github.app.name", "ReviewBotics"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("GitHub API response status for installation ID: {}", response.statusCode());
            log.debug("GitHub API response body: {}", response.body());
            
            if (response.statusCode() != 200) {
                log.error("Failed to get installation ID. Status: {}, Response: {}", response.statusCode(), response.body());
                return null;
            }

            Map<String, Object> installationData = objectMapper.readValue(response.body(), Map.class);
            String installationId = installationData.get("id").toString();
            log.debug("Found installation ID: {}", installationId);

            // Get installation token
            url = String.format("https://api.github.com/app/installations/%s/access_tokens", installationId);
            log.debug("Making request to get installation token: {}", url);
            
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/vnd.github.v3+json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("GitHub API response status for installation token: {}", response.statusCode());
            
            if (response.statusCode() != 201) {
                log.error("Failed to get installation token. Status: {}, Response: {}", response.statusCode(), response.body());
                return null;
            }

            Map<String, Object> tokenData = objectMapper.readValue(response.body(), Map.class);
            String token = (String) tokenData.get("token");
            
            if (token == null) {
                log.error("No token found in response for repository: {}", repositoryName);
                return null;
            }

            log.info("Successfully obtained installation token for repository: {}", repositoryName);
            return token;
        } catch (Exception e) {
            log.error("Error getting installation token for repository: {}", repositoryName, e);
            return null;
        }
    }

    @Override
    public CodeReview fetchPullRequestDetails(String repositoryName, String pullRequestId) {
        try {
            log.info("Fetching PR details for repository: {} and PR ID: {}", repositoryName, pullRequestId);
            
            String token = getInstallationToken(repositoryName);
            if (token == null) {
                log.error("Failed to get installation token for repository: {}", repositoryName);
                throw new RuntimeException("Failed to authenticate with GitHub. Please check your GitHub App credentials.");
            }
            
            String url = String.format("https://api.github.com/repos/%s/pulls/%s", repositoryName, pullRequestId);
            log.debug("Making request to GitHub API: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", environment.getProperty("github.app.name", "AI-Code-Review-Bot"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("GitHub API response status: {}", response.statusCode());
            
            if (response.statusCode() == 404) {
                log.error("Repository or PR not found. Repository: {}, PR ID: {}", repositoryName, pullRequestId);
                throw new RuntimeException("Repository or PR not found. Make sure the repository exists and the app has access.");
            }
            
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                log.error("Authentication failed. Status: {}, Response: {}", response.statusCode(), response.body());
                throw new RuntimeException("Authentication failed. Please check your GitHub App credentials and permissions.");
            }
            
            if (response.statusCode() != 200) {
                log.error("Failed to fetch PR details. Status: {}, Response: {}", response.statusCode(), response.body());
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
            review.setStatus(ReviewStatus.valueOf(((String) prData.get("state")).toUpperCase()));
            System.out.println("PrData-------"+prData);

            log.info("Successfully fetched PR details for repository: {} and PR ID: {}", repositoryName, pullRequestId);
            return review;
        } catch (Exception e) {
            log.error("Error fetching PR details for repository: {} and PR ID: {}", repositoryName, pullRequestId, e);
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

    @Override
    public String getPullRequestState(String repositoryName, String pullRequestId) {
        String url = String.format("https://api.github.com/repos/%s/pulls/%s", repositoryName, pullRequestId);
        var response = restTemplate.getForObject(url, Map.class);
        return (String) response.get("state");
    }
} 