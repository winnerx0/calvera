package com.winnerx0.calvera.projects;

import java.util.List;
import java.util.Optional;

public interface ProjectService {

    List<ProjectView> findAll(Long userId);

    Optional<ProjectView> findById(Long id, Long userId);

    Optional<ProjectView> findByRepositoryName(String repositoryName);

    Optional<Long> findOwnerUserId(Long projectId);

    ProjectView create(CreateProjectRequest request, Long userId);

    Optional<ProjectView> update(Long id, UpdateProjectRequest request, Long userId);

    boolean delete(Long id, Long userId);
}
