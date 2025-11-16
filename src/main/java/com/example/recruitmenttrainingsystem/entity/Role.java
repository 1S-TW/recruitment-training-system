package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "role_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    private String roleName;

    @Column(length = 255)
    private String description;
}