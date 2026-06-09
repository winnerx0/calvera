package com.winnerx0.calvera.apikey.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM ApiKey a
        WHERE a.apiKeyHash = :apiKeyHash
        AND a.deletedAt IS NULL
    )
    """)
    Boolean validate(String apiKeyHash);
}
