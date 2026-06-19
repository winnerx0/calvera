package com.winnerx0.calvera.reviews.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PrReviewRepository extends JpaRepository<PrReview, Long> {

    List<PrReview> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
