package com.winnerx0.calvera.webhook;

import com.winnerx0.calvera.common.ApiResponse;

import java.util.Optional;

public interface WebhookService {

    ApiResponse<String> createWebhook(Long projectId);

    Optional<String> getSecret(Long projectId);
}
