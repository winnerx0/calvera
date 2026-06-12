package com.winnerx0.calvera.events;

import java.util.List;
import java.util.Optional;

public interface CiEventService {

    CiEventView save(String deliveryId,
                     String repositoryFullName,
                     String workflowName,
                     String conclusion,
                     String jobsUrl,
                     String rawPayload,
                     Long projectId);

    Optional<CiEventView> findById(Long id);

    List<CiEventView> findAll();

    void updateStatusAnalyzing(Long id);

    void updateStatusDone(Long id, String analysisResult);

    void updateStatusFailed(Long id);
}
