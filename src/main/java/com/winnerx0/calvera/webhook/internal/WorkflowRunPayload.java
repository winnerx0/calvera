package com.winnerx0.calvera.webhook.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record WorkflowRunPayload(
        String action,
        @JsonProperty("workflow_run") WorkflowRun workflowRun,
        Repository repository
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WorkflowRun(
            String name,
            String conclusion,
            @JsonProperty("jobs_url") String jobsUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repository(
            @JsonProperty("full_name") String fullName
    ) {}
}
