package com.winnerx0.calvera.github.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.github.GithubRepoView;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private GithubRepoView toView(GithubRepoResponse repo) {
        return new GithubRepoView(repo.id(), repo.name(), repo.fullName(), repo.isPrivate(), repo.description(), repo.htmlUrl());
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
}
