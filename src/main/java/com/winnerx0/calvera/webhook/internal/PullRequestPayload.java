package com.winnerx0.calvera.webhook.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record PullRequestPayload(
        String action,
        int number,
        @JsonProperty("pull_request") PullRequest pullRequest,
        Repository repository) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PullRequest(
            String title,
            Ref head,
            Ref base,
            @JsonProperty("diff_url") String diffUrl,
            String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Ref(String sha, String ref) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repository(@JsonProperty("full_name") String fullName) {
    }
}
