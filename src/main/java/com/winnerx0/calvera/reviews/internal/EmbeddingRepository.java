package com.winnerx0.calvera.reviews.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface EmbeddingRepository extends JpaRepository<Embedding, Long> {

    @Query(value = """
            SELECT content
            FROM embeddings
            WHERE pr_review_id = :reviewId
              AND content IS NOT NULL
            ORDER BY embedding <=> CAST(:vector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findTopKContentByPrReviewId(
            @Param("reviewId") Long reviewId,
            @Param("vector") String vector,
            @Param("limit") int limit);
}
