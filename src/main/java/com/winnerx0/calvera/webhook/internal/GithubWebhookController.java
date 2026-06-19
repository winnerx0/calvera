package com.winnerx0.calvera.webhook.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.projects.ProjectView;
import com.winnerx0.calvera.reviews.PrReviewCreatedEvent;
import com.winnerx0.calvera.reviews.PrReviewService;
import com.winnerx0.calvera.reviews.PrReviewView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
class GithubWebhookController {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String IDEMPOTENCY_KEY_PREFIX = "webhook:delivery:";
    private static final Set<String> ANALYZED_ACTIONS = Set.of("opened", "synchronize", "reopened");

    private final WebhookSignatureVerifier signatureVerifier;
    private final PrReviewService prReviewService;
    private final ProjectService projectService;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestParam("projectId") Long projectId,
            @RequestBody byte[] rawBody) throws IOException {

        if (!signatureVerifier.verify(projectId, rawBody, signature)) {
            return ResponseEntity.status(401).build();
        }

        log.info("webhook starting");

        if (!"pull_request".equals(eventType)) {
            return ResponseEntity.ok().build();
        }

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + deliveryId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);
        if (Boolean.FALSE.equals(isNew)) {
            log.info("Duplicate delivery {}, skipping", deliveryId);
            return ResponseEntity.ok().build();
        }

        PullRequestPayload payload = objectMapper.readValue(rawBody, PullRequestPayload.class);
        PullRequestPayload.PullRequest pr = payload.pullRequest();

        if (payload.action() == null || pr == null) {
            return ResponseEntity.ok().build();
        }

        if (!ANALYZED_ACTIONS.contains(payload.action())) {
            return ResponseEntity.ok().build();
        }

        String repositoryFullName = payload.repository() != null ? payload.repository().fullName() : "unknown";

        Long resolvedProjectId = projectService.findByRepositoryName(repositoryFullName)
                .map(ProjectView::id)
                .orElse(null);

        if (resolvedProjectId == null) {
            log.warn("No project found for repository {}, dropping event", repositoryFullName);
            return ResponseEntity.ok().build();
        }

        String headSha = pr.head() != null ? pr.head().sha() : null;
        String baseSha = pr.base() != null ? pr.base().sha() : null;

        PrReviewView saved = prReviewService.save(
                deliveryId,
                repositoryFullName,
                payload.number(),
                pr.title(),
                payload.action(),
                headSha,
                baseSha,
                new String(rawBody),
                resolvedProjectId);

        eventPublisher.publishEvent(new PrReviewCreatedEvent(saved.id()));
        log.info("Persisted PR review {} for delivery {}", saved.id(), deliveryId);

        return ResponseEntity.ok().build();
    }
}
