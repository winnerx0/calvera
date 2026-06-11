package com.winnerx0.calvera.webhook.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnerx0.calvera.events.CiEventCreatedEvent;
import com.winnerx0.calvera.events.CiEventService;
import com.winnerx0.calvera.events.CiEventView;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.projects.ProjectView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
class GithubWebhookController {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String IDEMPOTENCY_KEY_PREFIX = "webhook:delivery:";

    private final WebhookSignatureVerifier signatureVerifier;
    private final CiEventService ciEventService;
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

        if (!"workflow_run".equals(eventType)) {
            return ResponseEntity.ok().build();
        }

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + deliveryId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate delivery {}, skipping", deliveryId);
            return ResponseEntity.ok().build();
        }

        WorkflowRunPayload payload = objectMapper.readValue(rawBody, WorkflowRunPayload.class);
        WorkflowRunPayload.WorkflowRun run = payload.workflowRun();

        if (run == null || run.conclusion() == null) {
            return ResponseEntity.ok().build();
        }

        String conclusion = run.conclusion();
        if (!"failure".equals(conclusion) && !"cancelled".equals(conclusion)) {
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

        CiEventView saved = ciEventService.save(
                deliveryId,
                repositoryFullName,
                run.name(),
                conclusion,
                run.logsUrl(),
                new String(rawBody),
                resolvedProjectId);

        eventPublisher.publishEvent(new CiEventCreatedEvent(saved.id()));
        log.info("Persisted CI event {} for delivery {}", saved.id(), deliveryId);

        return ResponseEntity.ok().build();
    }
}
