package com.winnerx0.calvera.reviews.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "embeddings")
@Getter
@Setter
@NoArgsConstructor
class Embedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "VECTOR(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pr_review_id", nullable = false)
    private PrReview prReview;
}
