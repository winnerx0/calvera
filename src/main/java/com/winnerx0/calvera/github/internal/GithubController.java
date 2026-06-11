package com.winnerx0.calvera.github.internal;

import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.github.GithubRepoView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
class GithubController {

    private final GithubConnectionService githubConnectionService;

    @GetMapping(value = "/repos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<GithubRepoView>>> getUserRepos(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(githubConnectionService.getUserRepos(userId)));
    }
}
