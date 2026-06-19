package com.winnerx0.calvera.analyzer.internal;

import com.winnerx0.calvera.analyzer.AiAssistant;
import com.winnerx0.calvera.reviews.BugFinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
class OpenAiClient implements AiAssistant {

    private static final String BUG_DETECTION_SYSTEM_PROMPT = """
            You are a senior software engineer doing a careful pull-request review. You will be \
            given the PR title and its unified diff. Identify likely BUGS introduced or left \
            unaddressed by this change — for example: null/undefined dereferences, off-by-one \
            and boundary errors, incorrect conditionals, resource leaks, race conditions, \
            unhandled errors, security issues (injection, missing authz, secret leakage), and \
            broken or missing edge-case handling.

            Rules:
            - Only report genuine, high-confidence problems. Do NOT report style, formatting, \
              naming, or subjective preferences.
            - Reference file paths exactly as they appear in the diff, and use line numbers from \
              the NEW version of the file (the `+` / right side of the diff).
            - For `startLine`/`endLine`, point at the changed line(s) where the bug lives.
            - `severity` must be one of: high, medium, low.
            - `category` must be one of: bug, security, logic, performance.
            - `suggestion` is optional; when you are confident, provide a minimal fix as a fenced \
              unified-diff block, otherwise leave it empty.
            - Write a short `summary` (2-4 sentences) describing the overall risk of the PR. If \
              you find no real issues, return an empty findings list and say so in the summary.
            """;

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final BeanOutputConverter<BugReport> outputConverter = new BeanOutputConverter<>(BugReport.class);

    OpenAiClient(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        this.embeddingModel = embeddingModel;
    }

    BugReport detectBugs(String prTitle, String diff) {
        String user = "PR title: " + (prTitle == null ? "(none)" : prTitle)
                + "\n\nUnified diff:\n" + diff
                + "\n\n" + outputConverter.getFormat();

        String content = chatClient.prompt()
                .system(BUG_DETECTION_SYSTEM_PROMPT)
                .user(user)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException(
                    "Model returned an empty response (likely token budget exhausted before output)");
        }

        try {
            BugReport report = outputConverter.convert(content);
            if (Objects.isNull(report)) {
                return new BugReport("Analysis produced no output.", List.of());
            }
            return new BugReport(
                    report.summary(),
                    report.findings() == null ? List.of() : report.findings());
        } catch (Exception e) {
            log.warn("Failed to parse bug detection output ({}): {}", e.getMessage(), truncate(content));
            throw new IllegalStateException("Could not parse model output as JSON", e);
        }
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    @Override
    public Flux<String> streamAnswer(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content();
    }

    @Override
    public String answer(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    List<Embedding> embedDiff(String diff){

        Document document = new Document(diff);

        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .withPunctuationMarks(List.of('.', '!', '?', ';', '\n'))
                .build();

        List<Document> chunks = textSplitter.apply(List.of(document));

        List<Embedding> embeddings = new ArrayList<>();

        for(var chunk : chunks){
            embeddings.add(new Embedding(chunk.getText(), this.embeddingModel.embed(chunk.getText())));
        }
        return embeddings;

    }

    private String truncate(String s) {
        return s.length() <= 500 ? s : s.substring(0, 500) + "…";
    }

    record BugReport(String summary, List<BugFinding> findings) {
    }

    record Embedding(String text, float[] embeddings){}
}
