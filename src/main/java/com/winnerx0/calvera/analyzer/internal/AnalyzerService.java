package com.winnerx0.calvera.analyzer.internal;

import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.reviews.BugFinding;
import com.winnerx0.calvera.reviews.EmbeddingChunk;
import com.winnerx0.calvera.reviews.PrReviewService;
import com.winnerx0.calvera.reviews.PrReviewView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class AnalyzerService {

    private final PrReviewService prReviewService;
    private final ProjectService projectService;
    private final GithubConnectionService githubConnectionService;
    private final GitHubPullRequestClient prClient;
    private final OpenAiClient openAiClient;

    void analyze(Long reviewId) {
        log.info("Starting analysis for PR review {}", reviewId);
        try {
            prReviewService.updateStatusAnalyzing(reviewId);

            PrReviewView review = prReviewService.findById(reviewId)
                    .orElseThrow(() -> new IllegalStateException("PrReview not found: " + reviewId));

            String accessToken = projectService.findOwnerUserId(review.projectId())
                    .flatMap(githubConnectionService::findAccessToken)
                    .orElseThrow(() -> new IllegalStateException(
                            "No GitHub access token found for project " + review.projectId()));

            String diff = prClient.fetchDiff(review.repositoryFullName(), review.prNumber(), accessToken)
                    .orElseThrow(() -> new IllegalStateException(
                            "Could not fetch diff for " + review.repositoryFullName() + "#" + review.prNumber()));

            List<EmbeddingChunk> chunks = openAiClient.embedDiff(diff).stream()
                    .map(e -> new EmbeddingChunk(e.text(), e.embeddings()))
                    .toList();
            prReviewService.saveEmbeddings(reviewId, chunks);

            OpenAiClient.BugReport report = openAiClient.detectBugs(review.prTitle(), diff);
            List<BugFinding> findings = report.findings();

            Long githubReviewId = postReview(review, findings, report.summary(), accessToken).orElse(null);

            prReviewService.updateStatusDone(reviewId, report.summary(), findings, githubReviewId);
            log.info("Analysis complete for PR review {} ({} findings, github review {})",
                    reviewId, findings.size(), githubReviewId);

        } catch (Exception e) {
            log.error("Analysis failed for PR review {}: {}", reviewId, e.getMessage(), e);
            prReviewService.updateStatusFailed(reviewId);
        }
    }

    /** Posting to GitHub is best-effort: a failure here must not fail the stored analysis. */
    private Optional<Long> postReview(PrReviewView review, List<BugFinding> findings,
                                      String summary, String accessToken) {
        try {
            List<GitHubPullRequestClient.PrFile> files =
                    prClient.fetchFiles(review.repositoryFullName(), review.prNumber(), accessToken);
            return prClient.postReview(review.repositoryFullName(), review.prNumber(), review.headSha(),
                    findings, summary, files, accessToken);
        } catch (Exception e) {
            log.warn("Failed to post GitHub review for PR review {}: {}", review.id(), e.getMessage());
            return Optional.empty();
        }
    }
}
