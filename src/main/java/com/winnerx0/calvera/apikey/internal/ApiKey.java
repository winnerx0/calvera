package com.winnerx0.calvera.apikey.internal;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String apiKeyHash;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(insertable = false)
    private LocalDate deletedAt;
}
