package com.winnerx0.calvera.auth.internal;

import com.winnerx0.calvera.apikey.ApiKeyService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationProvider(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String raw = (String) authentication.getCredentials();

        String encodedHex;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] rawHash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            encodedHex = HexFormat.of().formatHex(rawHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        boolean isValid = apiKeyService.validate(encodedHex);

        if(!isValid){
            throw new BadCredentialsException("Invalid api key");
        }

        return new ApiKeyAuthToken(raw, authentication.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiKeyAuthToken.class.isAssignableFrom(authentication);
    }
}
