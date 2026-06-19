package com.winnerx0.calvera.reviews.internal;

import com.winnerx0.calvera.analyzer.AiAssistant;
import com.winnerx0.calvera.projects.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class ReviewChatService {

    private static final int TOP_K = 6;

    private static final String SYSTEM_PROMPT = """
            You are a senior engineer helping the user reason about a specific pull request. \
            Use ONLY the diff context provided below to answer. If the answer is not present \
            in the context, say so plainly instead of guessing. Keep answers focused and cite \
            file paths from the diff when relevant.
            """;

    private final PrReviewRepository reviewRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ProjectService projectService;
    private final AiAssistant ai;

    Optional<String> answer(Long reviewId, Long userId, String question) {
        return buildUserPrompt(reviewId, userId, question)
                .map(prompt -> ai.answer(SYSTEM_PROMPT, prompt));
    }

    Optional<Flux<String>> streamAnswer(Long reviewId, Long userId, String question) {
        return buildUserPrompt(reviewId, userId, question)
                .map(prompt -> ai.streamAnswer(SYSTEM_PROMPT, prompt));
    }

    protected Optional<String> buildUserPrompt(Long reviewId, Long userId, String question) {
        return reviewRepository.findById(reviewId)
                .filter(r -> isOwner(r, userId))
                .map(review -> {
                    List<String> chunks = retrieveContext(reviewId, question);
                    return formatPrompt(review, chunks, question);
                });
    }

    private boolean isOwner(PrReview review, Long userId) {
        return projectService.findById(review.getProjectId(), userId).isPresent();
    }

    private List<String> retrieveContext(Long reviewId, String question) {
        float[] queryVector = ai.embed(question);
        String literal = toVectorLiteral(queryVector);
        return embeddingRepository.findTopKContentByPrReviewId(reviewId, literal, TOP_K);
    }

    private String formatPrompt(PrReview review, List<String> chunks, String question) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            context.append("--- Chunk ").append(i + 1).append(" ---\n")
                    .append(chunks.get(i)).append("\n\n");
        }
        if (chunks.isEmpty()) {
            context.append("(no diff chunks available)\n");
        }
        return "PR: " + review.getRepositoryFullName() + "#" + review.getPrNumber()
                + (review.getPrTitle() == null ? "" : " — " + review.getPrTitle())
                + "\n\nRelevant diff context:\n" + context
                + "\nQuestion: " + question;
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }
}
