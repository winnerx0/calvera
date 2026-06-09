package com.winnerx0.calvera.auth;

import com.winnerx0.calvera.apikey.ApiKeyService;
import com.winnerx0.calvera.auth.internal.ApiKeyAuthToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;

    public ApiKeyFilter(ApiKeyService apiKeyService, HandlerExceptionResolver handlerExceptionResolver, ApiKeyAuthenticationProvider apiKeyAuthenticationProvider) {
        this.apiKeyService = apiKeyService;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.apiKeyAuthenticationProvider = apiKeyAuthenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-Api-Key");

        if(apiKey == null || apiKey.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }

        try {

            if(SecurityContextHolder.getContext().getAuthentication() == null){

                Authentication authToken = apiKeyAuthenticationProvider.authenticate(new ApiKeyAuthToken(apiKey));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        } catch(Exception e){
            handlerExceptionResolver.resolveException(request, response, null, e);
            SecurityContextHolder.clearContext();
        }
    }
}
