package com.winnerx0.calvera.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AnalysisJobQueue {

    private final LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    public void enqueue(Long ciEventId) {
        queue.add(ciEventId);
    }

    public Long take() throws InterruptedException {
        return queue.take();
    }
}
