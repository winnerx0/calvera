package com.winnerx0.calvera.auth.internal;

import com.nimbusds.openid.connect.sdk.assurance.evidences.attachment.Digest;
import com.nimbusds.openid.connect.sdk.assurance.evidences.attachment.HashAlgorithm;
import com.winnerx0.calvera.apikey.ApiKeyService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationProvider(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String raw = (String) authentication.getCredentials();

        boolean isValid = apiKeyService.validate(raw);

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
