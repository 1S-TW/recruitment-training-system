package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationToken {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}