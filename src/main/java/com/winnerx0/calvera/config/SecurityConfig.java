package com.winnerx0.calvera.config;

import com.winnerx0.calvera.auth.OauthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, OauthSuccessHandler oauthSuccessHandler) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> {
                    oauth.successHandler(oauthSuccessHandler);
                    oauth.authorizationEndpoint(auth -> auth.baseUri("/oauth/login"));
                })
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain loginSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, OauthSuccessHandler oauthSuccessHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/login", "/oauth/**").permitAll().anyRequest().authenticated())
                .oauth2Login(oauth -> {
                    oauth.successHandler(oauthSuccessHandler);
                    oauth.authorizationEndpoint(auth -> auth.baseUri("/oauth/login"));
                })
                .build();
    }
}
