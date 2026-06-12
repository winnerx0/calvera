package com.winnerx0.calvera.auth.internal;

import com.winnerx0.calvera.auth.TokenPair;
import com.winnerx0.calvera.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Override
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {

        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadCredentialsException("Refresh token expired");
        }

        try {
            if (!"refresh".equals(jwtUtils.extractTokenType(rawRefreshToken))) {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            refreshTokenRepository.delete(stored);
            throw new BadCredentialsException("Invalid refresh token");
        }

        refreshTokenRepository.delete(stored);
        return createTokenPair(stored.getUserId());
    }
}
