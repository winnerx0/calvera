package com.winnerx0.calvera.auth;

public interface TokenService {

    TokenPair createTokenPair(Long userId);

    TokenPair refresh(String refreshToken);
}
