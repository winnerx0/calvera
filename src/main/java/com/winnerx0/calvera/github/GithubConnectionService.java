package com.winnerx0.calvera.github;

import java.util.List;
import java.util.Optional;

public interface GithubConnectionService {

    void saveOrUpdate(Long userId, String accessToken);

    List<GithubRepoView> getUserRepos(Long userId);

    List<PullRequestView> getOpenPullRequests(Long userId, String repoFullName);

    Optional<PullRequestView> getPullRequest(Long userId, String repoFullName, int number);

    Optional<String> findAccessToken(Long userId);
}
