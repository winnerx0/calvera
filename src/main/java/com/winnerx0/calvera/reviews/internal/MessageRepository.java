package com.winnerx0.calvera.reviews.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByPrReviewIdOrderByCreatedAtAsc(Long prReviewId);
}
