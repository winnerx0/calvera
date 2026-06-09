package com.winnerx0.calvera.github.internal;

import com.winnerx0.calvera.github.GithubConnectionService;
import org.springframework.stereotype.Service;

@Service
public class GithubConnectionServiceImpl implements GithubConnectionService {

    @Override
    public String getRefreshToken(String userId) {
        return "";
    }
}
