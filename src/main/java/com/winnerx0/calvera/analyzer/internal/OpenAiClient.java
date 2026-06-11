package com.winnerx0.calvera.analyzer.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class OpenAiClient {

    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_TOKENS = 1024;

    private final RestClient openAiRestClient;

    String analyze(String ciContext) {
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "max_tokens", MAX_TOKENS,
                "messages", List.of(
                        Map.of("role", "user", "content", buildPrompt(ciContext))
                )
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = openAiRestClient.post()
                .uri("/v1/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return "No analysis available.";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "No analysis available.";
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            return "No analysis available.";
        }

        return (String) message.get("content");
    }

    private String buildPrompt(String logs) {
        return """
                You are a CI/CD expert. Analyze the following GitHub Actions workflow failure logs \
                and provide a concise root cause analysis with actionable remediation steps.

                Keep your response under 500 words. Focus on:
                1. The primary cause of failure
                2. The specific file/line/command that failed
                3. Suggested fix

                Logs:
                """ + logs;
    }
}
