package com.winnerx0.calvera.reviews.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnerx0.calvera.reviews.BugFinding;
import com.winnerx0.calvera.reviews.EmbeddingChunk;
import com.winnerx0.calvera.reviews.PrReviewService;
import com.winnerx0.calvera.reviews.PrReviewView;
import com.winnerx0.calvera.reviews.ReviewStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class PrReviewServiceImpl implements PrReviewService {

    private static final TypeReference<List<BugFinding>> FINDINGS_TYPE = new TypeReference<>() {};

    private final PrReviewRepository repository;
    private final EmbeddingRepository embeddingRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PrReviewView save(String deliveryId, String repositoryFullName, int prNumber,
                             String prTitle, String action, String headSha, String baseSha,
                             String rawPayload, Long projectId) {
        PrReview entity = new PrReview();
        entity.setDeliveryId(deliveryId);
        entity.setRepositoryFullName(repositoryFullName);
        entity.setPrNumber(prNumber);
        entity.setPrTitle(prTitle);
        entity.setAction(action);
        entity.setHeadSha(headSha);
        entity.setBaseSha(baseSha);
        entity.setRawPayload(rawPayload);
        entity.setProjectId(projectId);
        return toView(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrReviewView> findById(Long id) {
        return repository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrReviewView> findAll() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrReviewView> findByProjectId(Long projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::toView).toList();
    }

    @Override
    @Transactional
    public void updateStatusAnalyzing(Long id) {
        repository.findById(id).ifPresent(e -> e.setStatus(ReviewStatus.ANALYZING));
    }

    @Override
    @Transactional
    public void updateStatusDone(Long id, String summary, List<BugFinding> findings, Long githubReviewId) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus(ReviewStatus.DONE);
            e.setSummary(summary);
            e.setFindings(writeFindings(findings));
            e.setGithubReviewId(githubReviewId);
        });
    }

    @Override
    @Transactional
    public void updateStatusFailed(Long id) {
        repository.findById(id).ifPresent(e -> e.setStatus(ReviewStatus.FAILED));
    }

    @Override
    @Transactional
    public void saveEmbeddings(Long reviewId, List<EmbeddingChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        PrReview reviewRef = repository.getReferenceById(reviewId);
        List<Embedding> entities = chunks.stream().map(c -> {
            Embedding e = new Embedding();
            e.setContent(c.content());
            e.setEmbedding(c.vector());
            e.setPrReview(reviewRef);
            return e;
        }).toList();
        embeddingRepository.saveAll(entities);
    }

    private String writeFindings(List<BugFinding> findings) {
        if (findings == null || findings.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(findings);
        } catch (Exception e) {
            log.warn("Failed to serialize findings: {}", e.getMessage());
            return null;
        }
    }

    private List<BugFinding> readFindings(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, FINDINGS_TYPE);
        } catch (Exception e) {
            log.warn("Failed to deserialize findings: {}", e.getMessage());
            return List.of();
        }
    }

    private PrReviewView toView(PrReview e) {
        return new PrReviewView(
                e.getId(), e.getDeliveryId(), e.getRepositoryFullName(),
                e.getPrNumber(), e.getPrTitle(), e.getAction(),
                e.getHeadSha(), e.getBaseSha(), e.getStatus(),
                e.getSummary(), readFindings(e.getFindings()), e.getGithubReviewId(),
                e.getProjectId(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
