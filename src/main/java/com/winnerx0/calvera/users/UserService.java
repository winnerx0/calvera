package com.winnerx0.calvera.users;

import com.winnerx0.calvera.auth.TokenPair;

public interface UserService {

    TokenPair findOrCreate(String username, String email, String picture, String githubAccessToken);
}
