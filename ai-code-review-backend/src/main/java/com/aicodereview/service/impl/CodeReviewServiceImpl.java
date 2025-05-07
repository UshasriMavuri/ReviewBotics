package com.aicodereview.service.impl;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewStatus;
import com.aicodereview.model.ReviewComment;
import com.aicodereview.service.AIReviewService;
import com.aicodereview.service.CodeReviewService;
import com.aicodereview.service.GitHubPRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final AIReviewService aiReviewService;
    private final GitHubPRService gitHubPRService;
    private final Map<Long, CodeReview> reviews = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public CodeReview createReview(CodeReview review) {
        review.setId(idGenerator.getAndIncrement());
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.PENDING);
        reviews.put(review.getId(), review);
        return review;
    }

    @Override
    public CodeReview getReview(Long id) {
        return Optional.ofNullable(reviews.get(id))
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    public List<CodeReview> getAllReviews() {
        return new ArrayList<>(reviews.values());
    }

    @Override
    public CodeReview updateReview(Long id, CodeReview review) {
        CodeReview existingReview = getReview(id);
        // Update fields
        existingReview.setTitle(review.getTitle());
        existingReview.setDescription(review.getDescription());
        existingReview.setComments(review.getComments());
        existingReview.setDocumentationSuggestions(review.getDocumentationSuggestions());
        existingReview.setTestSuggestions(review.getTestSuggestions());
        existingReview.setRefactoringSuggestions(review.getRefactoringSuggestions());
        existingReview.setQualityAnalysis(review.getQualityAnalysis());
        reviews.put(id, existingReview);
        return existingReview;
    }

    @Override
    public void deleteReview(Long id) {
        reviews.remove(id);
    }

    @Override
    public CodeReview reviewPullRequest(String repositoryName, String pullRequestId) {
        log.info("Starting review for PR {} in repository {}", pullRequestId, repositoryName);
        
        // Create new review
        CodeReview review = new CodeReview();
        review.setRepositoryName(repositoryName);
        review.setPullRequestId(pullRequestId);
        review.setStatus(ReviewStatus.IN_PROGRESS);
        review = createReview(review);

        try {
            // Get PR details
            CodeReview prDetails = gitHubPRService.fetchPullRequestDetails(repositoryName, pullRequestId);
            review.setTitle(prDetails.getTitle());
            review.setDescription(prDetails.getDescription());
            review.setAuthor(prDetails.getAuthor());
            review.setBaseBranch(prDetails.getBaseBranch());
            review.setHeadBranch(prDetails.getHeadBranch());
            review.setCommitSha(prDetails.getCommitSha());

            // Get PR diff
            String diff = gitHubPRService.getPullRequestDiff(repositoryName, pullRequestId);
            
            // Process with AI
            List<ReviewComment> comments = aiReviewService.reviewPullRequest(
                repositoryName,
                pullRequestId,
                "Full code review"
            );
            review.setComments(comments);

            // Get documentation suggestions
            List<String> docSuggestions = aiReviewService.generateDocumentationSuggestions(diff);
            review.setDocumentationSuggestions(String.join("\n", docSuggestions));

            // Get test suggestions
            List<String> testSuggestions = aiReviewService.generateTestSuggestions(diff);
            review.setTestSuggestions(String.join("\n", testSuggestions));

            // Get refactoring suggestions
            String refactoringSuggestions = aiReviewService.generateRefactoringSuggestions(diff);
            review.setRefactoringSuggestions(refactoringSuggestions);

            // Get code quality analysis
            Map<String, String> qualityAnalysis = aiReviewService.analyzeCodeQuality(diff);
            review.setQualityAnalysis(qualityAnalysis.toString());

            // Get suggested reviewers
            List<String> suggestedReviewers = gitHubPRService.suggestReviewers(repositoryName, pullRequestId);
            review.setSuggestedReviewers(String.join(",", suggestedReviewers));

            // Update status
            review.setStatus(ReviewStatus.COMPLETED);
            review.setCompletedAt(LocalDateTime.now());
            
            return updateReview(review.getId(), review);
        } catch (Exception e) {
            log.error("Error reviewing PR", e);
            review.setStatus(ReviewStatus.FAILED);
            updateReview(review.getId(), review);
            throw new RuntimeException("Failed to review PR", e);
        }
    }

    @Override
    public CodeReview updateReviewStatus(Long id, ReviewStatus status) {
        CodeReview review = getReview(id);
        review.setStatus(status);
        if (status == ReviewStatus.COMPLETED) {
            review.setCompletedAt(LocalDateTime.now());
        }
        return updateReview(id, review);
    }

    @Override
    public void processReview(Long id) {
        CodeReview review = getReview(id);
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new RuntimeException("Review is not in PENDING status");
        }
        updateReviewStatus(id, ReviewStatus.IN_PROGRESS);
        // Additional processing logic here
    }
} 