package com.winnerx0.calvera.projects;

public record CreateProjectRequest(
        String name,
        String repositoryName,
        Long repositoryId
) {}
