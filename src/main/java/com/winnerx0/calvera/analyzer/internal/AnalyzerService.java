package com.winnerx0.calvera.analyzer.internal;

import com.winnerx0.calvera.events.CiEventService;
import com.winnerx0.calvera.events.CiEventView;
import com.winnerx0.calvera.github.GithubConnectionService;
import com.winnerx0.calvera.projects.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class AnalyzerService {

    private final CiEventService ciEventService;
    private final ProjectService projectService;
    private final GithubConnectionService githubConnectionService;
    private final GitHubLogsClient logsClient;
    private final OpenAiClient openAiClient;

    void analyze(Long ciEventId) {
        log.info("Starting analysis for CI event {}", ciEventId);
        try {
            ciEventService.updateStatusAnalyzing(ciEventId);

            CiEventView event = ciEventService.findById(ciEventId)
                    .orElseThrow(() -> new IllegalStateException("CiEvent not found: " + ciEventId));

            String accessToken = projectService.findOwnerUserId(event.projectId())
                    .flatMap(githubConnectionService::findAccessToken)
                    .orElseThrow(() -> new IllegalStateException(
                            "No GitHub access token found for project " + event.projectId()));

            String logs = logsClient.fetchLogs(event.jobsUrl(), accessToken);
            String analysisResult = openAiClient.analyze(logs);

            ciEventService.updateStatusDone(ciEventId, analysisResult);
            log.info("Analysis complete for CI event {}", ciEventId);

        } catch (Exception e) {
            log.error("Analysis failed for CI event {}: {}", ciEventId, e.getMessage(), e);
            ciEventService.updateStatusFailed(ciEventId);
        }
    }
}
