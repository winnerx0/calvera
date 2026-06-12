package com.winnerx0.calvera.analyzer.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
class GitHubLogsClient {

    private static final int MAX_LOG_CHARS = 4000;
    private static final int CONTEXT_LINES = 3;
    private static final Pattern ERROR_PATTERN = Pattern.compile(
            "##\\[error]|##\\[warning]|\\berror\\b|\\bfailed\\b|\\bfailure\\b|\\bfatal\\b" +
            "|\\bexception\\b|\\bstacktrace\\b|npm err!|\\bat [a-z]",
            Pattern.CASE_INSENSITIVE
    );

    private final RestClient githubRestClient;

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JobsResponse(List<Job> jobs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Job(Long id, String name, String conclusion, String url) {}

    String fetchLogs(String jobsUrl, String accessToken) {
        JobsResponse response = githubRestClient.get()
                .uri(jobsUrl)
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .retrieve()
                .body(JobsResponse.class);

        if (response == null || response.jobs() == null || response.jobs().isEmpty()) {
            log.warn("No jobs found at {}", jobsUrl);
            return "";
        }

        List<Job> failedJobs = response.jobs().stream()
                .filter(job -> "failure".equals(job.conclusion()) || "cancelled".equals(job.conclusion()))
                .toList();

        List<Job> jobsToFetch = failedJobs.isEmpty() ? response.jobs() : failedJobs;

        StringBuilder sb = new StringBuilder();
        for (Job job : jobsToFetch) {
            try {
                String logs = fetchJobLogs(job.url() + "/logs", accessToken);
                sb.append("=== Job: ").append(job.name())
                        .append(" (").append(job.conclusion()).append(") ===\n")
                        .append(logs).append("\n");
            } catch (Exception e) {
                log.warn("Failed to fetch logs for job {} ({}): {}", job.name(), job.id(), e.getMessage());
            }
        }

        String filtered = filterErrorLines(sb.toString());
        log.info("Filtered logs ({} chars)", filtered.length());
        return filtered.length() > MAX_LOG_CHARS ? filtered.substring(filtered.length() - MAX_LOG_CHARS) : filtered;
    }

    private String filterErrorLines(String raw) {
        String[] lines = raw.split("\n", -1);
        boolean[] keep = new boolean[lines.length];

        for (int i = 0; i < lines.length; i++) {
            // Strip the leading timestamp GitHub prepends to every line before matching
            String content = lines[i].replaceFirst("^\\d{4}-\\d{2}-\\d{2}T[\\d:.]+Z\\s*", "");
            if (ERROR_PATTERN.matcher(content).find()) {
                int from = Math.max(0, i - CONTEXT_LINES);
                int to = Math.min(lines.length - 1, i + CONTEXT_LINES);
                for (int j = from; j <= to; j++) keep[j] = true;
            }
        }

        List<String> result = new ArrayList<>();
        boolean inGap = false;
        for (int i = 0; i < lines.length; i++) {
            if (keep[i]) {
                if (inGap) {
                    result.add("...");
                    inGap = false;
                }
                result.add(lines[i]);
            } else {
                inGap = true;
            }
        }
        return String.join("\n", result);
    }

    private String fetchJobLogs(String jobLogsUrl, String accessToken) {
        // GitHub responds 302 with a short-lived storage URL for the plain-text job log
        URI redirectUri = githubRestClient.get()
                .uri(jobLogsUrl)
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .exchange((req, res) -> {
                    if (res.getStatusCode() == HttpStatus.FOUND) {
                        URI location = res.getHeaders().getLocation();
                        if (location == null) {
                            throw new RuntimeException("GitHub job logs 302 had no Location header");
                        }
                        return location;
                    }
                    throw new RuntimeException("Expected 302 from GitHub job logs URL, got: " + res.getStatusCode());
                });

        // Download from storage without any auth headers
        String logs = RestClient.create()
                .get()
                .uri(redirectUri)
                .retrieve()
                .body(String.class);

        return logs == null ? "" : logs;
    }
}
