package com.winnerx0.calvera.auth.internal;

import com.winnerx0.calvera.auth.TokenPair;
import com.winnerx0.calvera.users.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
class OauthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        String accessToken = client.getAccessToken().getTokenValue();
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");
        String picture = oAuth2User.getAttribute("avatar_url");

        TokenPair tokenPair = userService.findOrCreate(username, email, picture, accessToken);

        String redirectUrl = frontendUrl + "/auth/callback"
                + "?accessToken=" + tokenPair.accessToken()
                + "&refreshToken=" + tokenPair.refreshToken();

        response.sendRedirect(redirectUrl);
    }
}
