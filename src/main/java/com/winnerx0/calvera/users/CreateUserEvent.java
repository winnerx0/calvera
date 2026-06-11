package com.winnerx0.calvera.users;

import java.time.LocalDateTime;

public record CreateUserEvent(
        String username,
        String email,
        String picture,
        LocalDateTime joinedAt) {
}
