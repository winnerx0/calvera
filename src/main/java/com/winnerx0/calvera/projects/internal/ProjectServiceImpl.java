package com.winnerx0.calvera.projects.internal;

import com.winnerx0.calvera.projects.CreateProjectRequest;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.projects.ProjectView;
import com.winnerx0.calvera.projects.UpdateProjectRequest;
import com.winnerx0.calvera.webhook.CreateWebhookSecretEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectView> findById(Long id, Long userId) {
        return projectRepository.findByIdAndUserId(id, userId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectView> findByRepositoryName(String repositoryName) {
        return projectRepository.findByRepositoryName(repositoryName).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findOwnerUserId(Long projectId) {
        return projectRepository.findById(projectId).map(Project::getUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectView> findAll(Long userId) {
        return projectRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public ProjectView create(CreateProjectRequest request, Long userId) {

        Project project = new Project();
        project.setName(request.name());
        project.setRepositoryName(request.repositoryName());
        project.setRepositoryId(request.repositoryId());
        project.setUserId(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        project = projectRepository.save(project);

        applicationEventPublisher.publishEvent(new CreateWebhookSecretEvent(project.getId()));

        return toView(project);
    }

    @Override
    @Transactional
    public Optional<ProjectView> update(Long id, UpdateProjectRequest request, Long userId) {

        return projectRepository.findByIdAndUserId(id, userId).map(project -> {
            project.setName(request.name());
            project.setUpdatedAt(LocalDateTime.now());

            return toView(projectRepository.save(project));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long userId) {

        return projectRepository.findByIdAndUserId(id, userId).map(project -> {
            projectRepository.delete(project);
            return true;
        }).orElse(false);
    }

    private ProjectView toView(Project project) {
        return new ProjectView(
                project.getId(),
                project.getName(),
                project.getRepositoryName(),
                project.getRepositoryId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
