package com.winnerx0.calvera.apikey.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
class ApiKey {
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
