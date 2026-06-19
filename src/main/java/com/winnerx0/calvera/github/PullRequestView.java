package com.winnerx0.calvera.github;

public record PullRequestView(
        int number,
        String title,
        String state,
        boolean draft,
        String headSha,
        String baseSha,
        String author,
        String htmlUrl
) {}
