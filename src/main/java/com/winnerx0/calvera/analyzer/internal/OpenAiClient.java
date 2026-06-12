package com.winnerx0.calvera.analyzer.internal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
class OpenAiClient {

    private static final String SYSTEM_PROMPT = """
            You are a CI/CD expert. Analyze the following GitHub Actions workflow failure logs \
            and provide a concise root cause analysis with actionable remediation steps.
            Keep your response under 500 words. Focus on:
            1. The primary cause of failure
            2. The specific file/line/command that failed
            3. Suggested fix
            """;

    private final ChatClient chatClient;

    OpenAiClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    String analyze(String ciContext) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(ciContext)
                .call()
                .content();
    }
}
