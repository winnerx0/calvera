package com.winnerx0.calvera.webhook.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookSecretRepository extends JpaRepository<WebhookSecret, Long> {

    Optional<WebhookSecret> findByProjectId(Long projectId);
}
