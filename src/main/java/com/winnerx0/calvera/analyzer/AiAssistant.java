package com.winnerx0.calvera.analyzer;

import reactor.core.publisher.Flux;

public interface AiAssistant {

    float[] embed(String text);

    String answer(String systemPrompt, String userPrompt);

    Flux<String> streamAnswer(String systemPrompt, String userPrompt);
}
