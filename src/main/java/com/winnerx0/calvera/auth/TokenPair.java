package com.winnerx0.calvera.auth;

public record TokenPair(
        String accessToken,
        String refreshToken
) {}
