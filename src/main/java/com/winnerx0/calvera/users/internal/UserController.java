package com.winnerx0.calvera.users.internal;

import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.users.UpdateUserRequest;
import com.winnerx0.calvera.users.UserService;
import com.winnerx0.calvera.users.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserView>> me(Authentication authentication) {
        return userService.findById(userId(authentication))
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found")));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserView>> update(@RequestBody UpdateUserRequest request, Authentication authentication) {
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username is required"));
        }
        return userService.updateUsername(userId(authentication), request.username().trim())
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found")));
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
