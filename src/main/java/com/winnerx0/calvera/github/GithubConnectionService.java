package com.winnerx0.calvera.github;

import java.util.List;
import java.util.Optional;

public interface GithubConnectionService {

    void saveOrUpdate(Long userId, String accessToken);

    List<GithubRepoView> getUserRepos(Long userId);

    Optional<String> findAccessToken(Long userId);
}
