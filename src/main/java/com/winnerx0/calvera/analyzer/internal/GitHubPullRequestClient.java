package com.winnerx0.calvera.analyzer.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.winnerx0.calvera.reviews.BugFinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
class GitHubPullRequestClient {

    private static final int MAX_DIFF_CHARS = 30_000;

    private final RestClient githubRestClient;

    /** Raw unified diff of the PR, truncated to {@link #MAX_DIFF_CHARS}, or empty if unavailable. */
    Optional<String> fetchDiff(String repoFullName, int prNumber, String accessToken) {
        try {
            String[] parts = repoFullName.split("/");
            String diff = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{number}", parts[0], parts[1], prNumber)
                    .headers(h -> {
                        h.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                        h.set(HttpHeaders.ACCEPT, "application/vnd.github.diff");
                    })
                    .retrieve()
                    .body(String.class);

            if (diff == null || diff.isBlank()) {
                return Optional.empty();
            }
            if (diff.length() > MAX_DIFF_CHARS) {
                log.info("PR {}#{} diff is {} chars, truncating to {}", repoFullName, prNumber, diff.length(), MAX_DIFF_CHARS);
                diff = diff.substring(0, MAX_DIFF_CHARS) + "\n... [diff truncated] ...";
            }
            return Optional.of(diff);
        } catch (Exception e) {
            log.warn("Failed to fetch diff for {}#{}: {}", repoFullName, prNumber, e.getMessage());
            return Optional.empty();
        }
    }

    /** Changed files with their unified-diff hunks, used to map findings to commentable lines. */
    List<PrFile> fetchFiles(String repoFullName, int prNumber, String accessToken) {
        try {
            String[] parts = repoFullName.split("/");
            PrFile[] files = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{number}/files?per_page=100", parts[0], parts[1], prNumber)
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .retrieve()
                    .body(PrFile[].class);
            return files == null ? List.of() : List.of(files);
        } catch (Exception e) {
            log.warn("Failed to fetch files for {}#{}: {}", repoFullName, prNumber, e.getMessage());
            return List.of();
        }
    }

    Optional<Long> postReview(String repoFullName, int prNumber, String headSha,
                              List<BugFinding> findings, String summary,
                              List<PrFile> files, String accessToken) {
        try {
            Map<String, Set<Integer>> commentable = new HashMap<>();
            for (PrFile f : files) {
                if (f.patch() != null) {
                    commentable.put(f.filename(), commentableLines(f.patch()));
                }
            }

            List<Map<String, Object>> comments = new ArrayList<>();
            List<BugFinding> unmapped = new ArrayList<>();
            for (BugFinding finding : findings) {
                Set<Integer> lines = commentable.get(finding.file());
                if (lines != null && lines.contains(finding.startLine())) {
                    Map<String, Object> comment = new HashMap<>();
                    comment.put("path", finding.file());
                    comment.put("line", finding.startLine());
                    comment.put("side", "RIGHT");
                    comment.put("body", commentBody(finding));
                    comments.add(comment);
                } else {
                    unmapped.add(finding);
                }
            }

            Map<String, Object> body = new HashMap<>();
            if (headSha != null) {
                body.put("commit_id", headSha);
            }
            body.put("event", "COMMENT");
            body.put("body", reviewBody(summary, unmapped));
            if (!comments.isEmpty()) {
                body.put("comments", comments);
            }

            String[] parts = repoFullName.split("/");
            ReviewResponse response = githubRestClient.post()
                    .uri("/repos/{owner}/{repo}/pulls/{number}/reviews", parts[0], parts[1], prNumber)
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .body(body)
                    .retrieve()
                    .body(ReviewResponse.class);

            return response == null ? Optional.empty() : Optional.ofNullable(response.id());
        } catch (Exception e) {
            log.warn("Failed to post review for {}#{}: {}", repoFullName, prNumber, e.getMessage());
            return Optional.empty();
        }
    }

    /** New-file (RIGHT side) line numbers that appear in the patch and can carry inline comments. */
    private Set<Integer> commentableLines(String patch) {
        Set<Integer> lines = new HashSet<>();
        int newLine = 0;
        for (String line : patch.split("\n", -1)) {
            if (line.startsWith("@@")) {
                // @@ -a,b +c,d @@
                int plus = line.indexOf('+');
                int space = line.indexOf(' ', plus);
                if (plus >= 0 && space > plus) {
                    String range = line.substring(plus + 1, space);
                    String start = range.contains(",") ? range.substring(0, range.indexOf(',')) : range;
                    try {
                        newLine = Integer.parseInt(start.trim());
                    } catch (NumberFormatException ignored) {
                        newLine = 0;
                    }
                }
            } else if (line.startsWith("+")) {
                lines.add(newLine);
                newLine++;
            } else if (line.startsWith("-")) {
                // deletion: does not advance the new-file counter
            } else {
                // context line
                lines.add(newLine);
                newLine++;
            }
        }
        return lines;
    }

    private String commentBody(BugFinding f) {
        StringBuilder sb = new StringBuilder();
        sb.append("**[").append(f.severity()).append("] ").append(f.title()).append("**\n\n")
                .append(f.description());
        if (f.suggestion() != null && !f.suggestion().isBlank()) {
            sb.append("\n\n").append(f.suggestion());
        }
        return sb.toString();
    }

    private String reviewBody(String summary, List<BugFinding> unmapped) {
        StringBuilder sb = new StringBuilder("## Calvera PR review\n\n");
        if (summary != null && !summary.isBlank()) {
            sb.append(summary).append("\n\n");
        }
        if (!unmapped.isEmpty()) {
            sb.append("### Additional findings\n\n");
            for (BugFinding f : unmapped) {
                sb.append("- **[").append(f.severity()).append("] ")
                        .append(f.file()).append(":").append(f.startLine()).append("** — ")
                        .append(f.title()).append("\n");
            }
        }
        return sb.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PrFile(String filename, String patch, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ReviewResponse(Long id) {
    }
}
