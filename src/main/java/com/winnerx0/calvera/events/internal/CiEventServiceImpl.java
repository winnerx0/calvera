package com.winnerx0.calvera.events.internal;

import com.winnerx0.calvera.events.CiEventService;
import com.winnerx0.calvera.events.CiEventStatus;
import com.winnerx0.calvera.events.CiEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class CiEventServiceImpl implements CiEventService {

    private final CiEventRepository repository;

    @Override
    @Transactional
    public CiEventView save(String deliveryId, String repositoryFullName,
                            String workflowName, String conclusion,
                            String logsUrl, String rawPayload, Long projectId) {
        CiEvent entity = new CiEvent();
        entity.setDeliveryId(deliveryId);
        entity.setRepositoryFullName(repositoryFullName);
        entity.setWorkflowName(workflowName);
        entity.setConclusion(conclusion);
        entity.setLogsUrl(logsUrl);
        entity.setRawPayload(rawPayload);
        entity.setProjectId(projectId);
        return toView(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CiEventView> findById(Long id) {
        return repository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiEventView> findAll() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional
    public void updateStatusAnalyzing(Long id) {
        repository.findById(id).ifPresent(e -> e.setStatus(CiEventStatus.ANALYZING));
    }

    @Override
    @Transactional
    public void updateStatusDone(Long id, String analysisResult) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus(CiEventStatus.DONE);
            e.setAnalysisResult(analysisResult);
        });
    }

    @Override
    @Transactional
    public void updateStatusFailed(Long id) {
        repository.findById(id).ifPresent(e -> e.setStatus(CiEventStatus.FAILED));
    }

    private CiEventView toView(CiEvent e) {
        return new CiEventView(
                e.getId(), e.getDeliveryId(), e.getRepositoryFullName(),
                e.getWorkflowName(), e.getConclusion(), e.getLogsUrl(),
                e.getStatus(), e.getAnalysisResult(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
