package com.winnerx0.calvera.reviews;

import java.time.LocalDateTime;

public record MessageView(
        Long id,
        Long prReviewId,
        String role,
        String content,
        LocalDateTime createdAt
) {}
