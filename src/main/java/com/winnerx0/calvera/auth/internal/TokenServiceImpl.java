package com.winnerx0.calvera.auth.internal;

import com.winnerx0.calvera.auth.TokenPair;
import com.winnerx0.calvera.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class TokenServiceImpl implements TokenService {

    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public TokenPair createTokenPair(Long userId) {

        String accessToken = jwtUtils.generateAccessToken(userId);
        String rawRefreshToken = jwtUtils.generateRefreshToken(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(rawRefreshToken);
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));

        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, rawRefreshToken);
    }
}
