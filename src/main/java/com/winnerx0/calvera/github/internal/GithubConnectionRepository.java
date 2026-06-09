package com.winnerx0.calvera.github.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubConnectionRepository extends JpaRepository<GithubConnection, Long> {
}
