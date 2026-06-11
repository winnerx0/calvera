package com.winnerx0.calvera.github;

public record GithubRepoView(
        Long id,
        String name,
        String fullName,
        boolean isPrivate,
        String description,
        String htmlUrl
) {}
