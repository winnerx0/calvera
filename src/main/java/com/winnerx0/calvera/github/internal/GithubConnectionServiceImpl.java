package com.winnerx0.calvera.github.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.github.GithubRepoView;
import com.winnerx0.calvera.github.PullRequestView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class GithubConnectionServiceImpl implements GithubConnectionService {

    private final GithubConnectionRepository githubConnectionRepository;
    private final RestClient githubRestClient;

    @Override
    @Transactional
    public void saveOrUpdate(Long userId, String accessToken) {

        GithubConnection connection = githubConnectionRepository.findByUserId(userId)
                .orElseGet(() -> {
                    GithubConnection c = new GithubConnection();
                    c.setUserId(userId);
                    c.setCreatedAt(LocalDateTime.now());
                    return c;
                });

        connection.setAccessToken(accessToken);
        connection.setUpdatedAt(LocalDateTime.now());
        githubConnectionRepository.save(connection);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findAccessToken(Long userId) {
        return githubConnectionRepository.findByUserId(userId).map(GithubConnection::getAccessToken);
    }

    @Override
    public List<GithubRepoView> getUserRepos(Long userId) {

        String accessToken = githubConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No GitHub connection found for user"))
                .getAccessToken();

        List<GithubRepoResponse> repos = githubRestClient.get()
                .uri("/user/repos?per_page=100&sort=updated")
                // set() replaces the bean's default PAT header; header() would add a second Authorization value
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return repos == null ? List.of() : repos.stream().map(this::toView).toList();
    }

    @Override
    public List<PullRequestView> getOpenPullRequests(Long userId, String repoFullName) {
        String accessToken = accessTokenOrThrow(userId);
        String[] parts = repoFullName.split("/");

        List<PullRequestResponse> pulls = githubRestClient.get()
                .uri("/repos/{owner}/{repo}/pulls?state=open&per_page=100&sort=updated&direction=desc",
                        parts[0], parts[1])
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return pulls == null ? List.of() : pulls.stream().map(this::toView).toList();
    }

    @Override
    public Optional<PullRequestView> getPullRequest(Long userId, String repoFullName, int number) {
        String accessToken = accessTokenOrThrow(userId);
        String[] parts = repoFullName.split("/");
        try {
            PullRequestResponse pr = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{number}", parts[0], parts[1], number)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .retrieve()
                    .body(PullRequestResponse.class);
            return Optional.ofNullable(pr).map(this::toView);
        } catch (Exception e) {
            log.warn("Failed to fetch PR {}#{}: {}", repoFullName, number, e.getMessage());
            return Optional.empty();
        }
    }

    private String accessTokenOrThrow(Long userId) {
        return githubConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No GitHub connection found for user"))
                .getAccessToken();
    }

    private GithubRepoView toView(GithubRepoResponse repo) {
        return new GithubRepoView(repo.id(), repo.name(), repo.fullName(), repo.isPrivate(), repo.description(), repo.htmlUrl());
    }

    private PullRequestView toView(PullRequestResponse pr) {
        return new PullRequestView(
                pr.number(),
                pr.title(),
                pr.state(),
                pr.draft(),
                pr.head() != null ? pr.head().sha() : null,
                pr.base() != null ? pr.base().sha() : null,
                pr.user() != null ? pr.user().login() : null,
                pr.htmlUrl());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GithubRepoResponse(
            Long id,
            String name,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("private") boolean isPrivate,
            String description,
            @JsonProperty("html_url") String htmlUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PullRequestResponse(
            int number,
            String title,
            String state,
            boolean draft,
            Ref head,
            Ref base,
            User user,
            @JsonProperty("html_url") String htmlUrl
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Ref(String sha) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record User(String login) {}
    }
}
