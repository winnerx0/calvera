package com.winnerx0.calvera.projects;

import java.time.LocalDateTime;

public record ProjectView(
        Long id,
        String name,
        String repositoryName,
        Long repositoryId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
