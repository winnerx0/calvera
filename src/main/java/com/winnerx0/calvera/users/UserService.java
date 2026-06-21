package com.winnerx0.calvera.users;

import com.winnerx0.calvera.auth.TokenPair;

import java.util.Optional;

public interface UserService {

    TokenPair findOrCreate(String username, String email, String picture, String githubAccessToken);

    Optional<UserView> findById(Long id);

    Optional<UserView> updateUsername(Long id, String username);
}
