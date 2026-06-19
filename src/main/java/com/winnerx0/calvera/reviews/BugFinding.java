package com.winnerx0.calvera.reviews;

public record BugFinding(
        String file,
        int startLine,
        int endLine,
        String severity,
        String category,
        String title,
        String description,
        String suggestion
) {}
