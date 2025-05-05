package com.aicodereview.service.impl;

import com.aicodereview.model.CodeReview;
import com.aicodereview.model.ReviewStatus;
import com.aicodereview.repository.CodeReviewRepository;
import com.aicodereview.service.CodeReviewService;
import com.aicodereview.service.GitHubService;
import com.aicodereview.service.AIReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final CodeReviewRepository codeReviewRepository;
    private final GitHubService gitHubService;
    private final AIReviewService aiReviewService;

    @Override
    @Transactional
    public CodeReview createReview(String repositoryName, String pullRequestId, String commitSha) {
        CodeReview review = new CodeReview();
        review.setRepositoryName(repositoryName);
        review.setPullRequestId(pullRequestId);
        review.setCommitSha(commitSha);
        review.setStatus(ReviewStatus.PENDING);
        return codeReviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public CodeReview getReview(Long id) {
        return codeReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }

    @Override
    @Transactional
    public CodeReview updateReviewStatus(Long id, ReviewStatus status) {
        CodeReview review = getReview(id);
        review.setStatus(status);
        return codeReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void processReview(Long id) {
        CodeReview review = getReview(id);
        try {
            review.setStatus(ReviewStatus.IN_PROGRESS);
            codeReviewRepository.save(review);

            // Get PR diff from GitHub
            String diff = gitHubService.getPullRequestDiff(
                review.getRepositoryName(),
                review.getPullRequestId()
            );

            // Process with AI
            aiReviewService.analyzeCode(diff, review);

            review.setStatus(ReviewStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Error processing review: " + id, e);
            review.setStatus(ReviewStatus.FAILED);
        }
        codeReviewRepository.save(review);
    }
} 