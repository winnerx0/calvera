package com.winnerx0.calvera.github;

import java.util.List;

public interface GithubConnectionService {

    void saveOrUpdate(Long userId, String accessToken);

    List<GithubRepoView> getUserRepos(Long userId);
}
