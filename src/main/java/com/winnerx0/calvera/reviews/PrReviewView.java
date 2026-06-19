package com.winnerx0.calvera.reviews;

import java.time.LocalDateTime;
import java.util.List;

public record PrReviewView(
        Long id,
        String deliveryId,
        String repositoryFullName,
        int prNumber,
        String prTitle,
        String action,
        String headSha,
        String baseSha,
        ReviewStatus status,
        String summary,
        List<BugFinding> findings,
        Long githubReviewId,
        Long projectId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
