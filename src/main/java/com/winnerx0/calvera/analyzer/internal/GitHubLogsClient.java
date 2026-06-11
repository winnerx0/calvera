package com.winnerx0.calvera.analyzer.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
class GitHubLogsClient {

    private final RestClient githubRestClient;

    String fetchLogs(String logsUrl) {
        // Step 1: follow the GitHub API URL to get the S3 redirect Location
        URI redirectUri = githubRestClient.get()
                .uri(logsUrl)
                .exchange((req, res) -> {
                    if (res.getStatusCode() == HttpStatus.FOUND) {
                        URI location = res.getHeaders().getLocation();
                        if (location == null) {
                            throw new RuntimeException("GitHub logs 302 had no Location header");
                        }
                        return location;
                    }
                    throw new RuntimeException("Expected 302 from GitHub logs URL, got: " + res.getStatusCode());
                });

        // Step 2: download the zip from S3 without any auth headers
        byte[] zipBytes = RestClient.create()
                .get()
                .uri(redirectUri)
                .retrieve()
                .body(byte[].class);

        if (zipBytes == null || zipBytes.length == 0) {
            return "";
        }
        return extractAndTruncateLogs(zipBytes);
    }

    private String extractAndTruncateLogs(byte[] zipBytes) {
        StringBuilder sb = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = zis.readAllBytes();
                    sb.append("=== ").append(entry.getName()).append(" ===\n");
                    sb.append(new String(content, StandardCharsets.UTF_8)).append("\n");
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            log.warn("Error parsing zip logs: {}", e.getMessage());
        }
        String full = sb.toString();
        // Keep the last 4000 chars — most recent output is most relevant for failures
        return full.length() > 4000 ? full.substring(full.length() - 4000) : full;
    }
}
