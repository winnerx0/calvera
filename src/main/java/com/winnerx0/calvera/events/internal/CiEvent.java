package com.winnerx0.calvera.events.internal;

import com.winnerx0.calvera.events.CiEventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "ci_events")
@Getter
@Setter
@NoArgsConstructor
class CiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false)
    private String repositoryFullName;

    @Column(nullable = false)
    private String workflowName;

    @Column(nullable = false)
    private String conclusion;

    @Column(nullable = false)
    private String jobsUrl;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CiEventStatus status = CiEventStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String analysisResult;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long projectId;
}
