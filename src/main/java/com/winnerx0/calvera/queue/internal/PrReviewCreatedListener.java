package com.winnerx0.calvera.queue.internal;

import com.winnerx0.calvera.queue.AnalysisJobQueue;
import com.winnerx0.calvera.reviews.PrReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class PrReviewCreatedListener {

    private final AnalysisJobQueue analysisJobQueue;

    @EventListener
    public void onPrReviewCreated(PrReviewCreatedEvent event) {
        log.info("Enqueueing PR review {} for analysis", event.reviewId());
        analysisJobQueue.enqueue(event.reviewId());
    }
}
