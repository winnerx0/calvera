package com.winnerx0.calvera.analyzer.internal;

import com.winnerx0.calvera.queue.AnalysisJobQueue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
class WorkerPool {

    private static final int WORKER_COUNT = 3;

    private final AnalysisJobQueue analysisJobQueue;
    private final AnalyzerService analyzerService;

    private ExecutorService executorService;

    @PostConstruct
    void start() {
        executorService = Executors.newFixedThreadPool(
                WORKER_COUNT,
                Thread.ofVirtual().name("analyzer-worker-", 0).factory());

        for (int i = 0; i < WORKER_COUNT; i++) {
            executorService.submit(this::workerLoop);
        }
        log.info("Started {} analyzer worker threads", WORKER_COUNT);
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Long id = analysisJobQueue.take();
                analyzerService.analyze(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Analyzer worker interrupted, shutting down");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in worker loop: {}", e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    void shutdown() {
        log.info("Shutting down analyzer worker pool");
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Worker pool did not terminate within 10 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
