package com.winnerx0.calvera.queue.internal;

import com.winnerx0.calvera.events.CiEventCreatedEvent;
import com.winnerx0.calvera.queue.AnalysisJobQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class CiEventCreatedListener {

    private final AnalysisJobQueue analysisJobQueue;

    @EventListener
    public void onCiEventCreated(CiEventCreatedEvent event) {
        log.info("Enqueueing CI event {} for analysis", event.ciEventId());
        analysisJobQueue.enqueue(event.ciEventId());
    }
}
