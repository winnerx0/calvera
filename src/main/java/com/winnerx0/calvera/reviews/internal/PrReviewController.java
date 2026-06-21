package com.winnerx0.calvera.reviews.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.github.PullRequestView;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.projects.ProjectView;
import com.winnerx0.calvera.reviews.PrReviewCreatedEvent;
import com.winnerx0.calvera.reviews.MessageView;
import com.winnerx0.calvera.reviews.PrReviewService;
import com.winnerx0.calvera.reviews.PrReviewView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
class PrReviewController {

    private final PrReviewService prReviewService;
    private final ProjectService projectService;
    private final GithubConnectionService githubConnectionService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReviewChatService chatService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrReviewView>>> getAll(
            @RequestParam(value = "projectId", required = false) Long projectId,
            Authentication authentication) {
        if (projectId != null) {
            Long userId = userId(authentication);
            if (projectService.findById(projectId, userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found"));
            }
            return ResponseEntity.ok(ApiResponse.ok(prReviewService.findByProjectId(projectId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(prReviewService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrReviewView>> getById(@PathVariable Long id) {
        return prReviewService.findById(id)
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found")));
    }

    @GetMapping("/pulls")
    public ResponseEntity<ApiResponse<List<PullRequestView>>> listPulls(
            @RequestParam("projectId") Long projectId, Authentication authentication) {
        Long userId = userId(authentication);
        Optional<ProjectView> project = projectService.findById(projectId, userId);

        if (project.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found"));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                githubConnectionService.getOpenPullRequests(userId, project.get().repositoryName())));
    }

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<PrReviewView>> trigger(@RequestBody TriggerReviewRequest request, Authentication authentication) {

        Long userId = userId(authentication);
        Optional<ProjectView> project = projectService.findById(request.projectId(), userId);

        if (project.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found"));
        }

        String repositoryFullName = project.get().repositoryName();
        Optional<PullRequestView> pr = githubConnectionService.getPullRequest(userId, repositoryFullName, request.prNumber());

        if (pr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Pull request not found"));
        }

        PullRequestView view = pr.get();
        String deliveryId = "manual:" + request.projectId() + ":" + view.number() + ":" + System.currentTimeMillis();

        PrReviewView saved = prReviewService.save(
                deliveryId,
                repositoryFullName,
                view.number(),
                view.title(),
                "manual",
                view.headSha(),
                view.baseSha(),
                null,
                project.get().id());

        eventPublisher.publishEvent(new PrReviewCreatedEvent(saved.id()));
        log.info("Manually queued PR review {} for {}#{}", saved.id(), repositoryFullName, view.number());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(saved));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageView>>> getMessages(
            @PathVariable Long id, Authentication authentication) {
        return prReviewService.findById(id)
                .filter(r -> projectService.findById(r.projectId(), userId(authentication)).isPresent())
                .map(r -> ResponseEntity.ok(ApiResponse.ok(prReviewService.findMessages(id))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found")));
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(
            @PathVariable Long id,
            @RequestBody ChatRequest request,
            Authentication authentication) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Question is required"));
        }
        return chatService.answer(id, userId(authentication), request.question())
                .map(answer -> ResponseEntity.ok(ApiResponse.ok(Map.of("answer", answer))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found")));
    }

    @GetMapping(value = "/{id}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@PathVariable Long id, @RequestParam("question") String question, Authentication authentication) {
        SseEmitter emitter = new SseEmitter(0L);
        if (question == null || question.isBlank()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("Question is required"));
            } catch (IOException ignored) {}
            emitter.complete();
            return emitter;
        }

        Optional<Flux<String>> stream = chatService.streamAnswer(id, userId(authentication), question);

        if (stream.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("Review not found"));
            } catch (IOException ignored) {}
            emitter.complete();
            return emitter;
        }

        stream.get().subscribe(
                token -> {
                    try {
                        String json = objectMapper.writeValueAsString(token);
                        emitter.send(SseEmitter.event().name("token").data(json));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data(""));
                    } catch (IOException ignored) {}
                    emitter.complete();
                });
        return emitter;
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    record ChatRequest(String question) {}
}
