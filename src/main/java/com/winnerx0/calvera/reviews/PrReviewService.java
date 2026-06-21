package com.winnerx0.calvera.reviews;

import java.util.List;
import java.util.Optional;

public interface PrReviewService {

    PrReviewView save(String deliveryId,
                      String repositoryFullName,
                      int prNumber,
                      String prTitle,
                      String action,
                      String headSha,
                      String baseSha,
                      String rawPayload,
                      Long projectId);

    Optional<PrReviewView> findById(Long id);

    List<PrReviewView> findAll();

    List<PrReviewView> findByProjectId(Long projectId);

    void updateStatusAnalyzing(Long id);

    void updateStatusDone(Long id, String summary, List<BugFinding> findings, Long githubReviewId);

    void updateStatusFailed(Long id);

    void saveEmbeddings(Long reviewId, List<EmbeddingChunk> chunks);

    List<MessageView> findMessages(Long reviewId);

    MessageView saveMessage(Long reviewId, String role, String content);
}
