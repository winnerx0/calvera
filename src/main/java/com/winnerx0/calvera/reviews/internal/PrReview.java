package com.winnerx0.calvera.reviews.internal;

import com.winnerx0.calvera.reviews.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pr_reviews")
@Getter
@Setter
@NoArgsConstructor
class PrReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false)
    private String repositoryFullName;

    @Column(nullable = false)
    private int prNumber;

    @Column(length = 512)
    private String prTitle;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 40)
    private String headSha;

    @Column(length = 40)
    private String baseSha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String findings;

    private Long githubReviewId;

    @OneToMany(mappedBy = "prReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Embedding> embeddings = new ArrayList<>();

    @OneToMany(mappedBy = "prReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long projectId;
}
