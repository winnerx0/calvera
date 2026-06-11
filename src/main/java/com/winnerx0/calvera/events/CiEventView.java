package com.winnerx0.calvera.events;

import java.time.Instant;
import java.time.LocalDateTime;

public record CiEventView(
        Long id,
        String deliveryId,
        String repositoryFullName,
        String workflowName,
        String conclusion,
        String logsUrl,
        CiEventStatus status,
        String analysisResult,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
