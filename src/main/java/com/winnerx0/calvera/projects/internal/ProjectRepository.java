package com.winnerx0.calvera.projects.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndUserId(Long id, Long userId);

    List<Project> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Project> findByRepositoryName(String repositoryName);
}
