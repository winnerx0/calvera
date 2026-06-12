package com.winnerx0.calvera.auth.internal;

import com.winnerx0.calvera.auth.TokenPair;
import com.winnerx0.calvera.auth.TokenService;
import com.winnerx0.calvera.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final TokenService tokenService;

    record RefreshRequest(String refreshToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPair>> refresh(@RequestBody RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh token is required"));
        }
        return ResponseEntity.ok(ApiResponse.ok(tokenService.refresh(request.refreshToken())));
    }
}
