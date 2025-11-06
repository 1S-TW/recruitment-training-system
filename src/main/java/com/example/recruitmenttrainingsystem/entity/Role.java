package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 1..5

    @Column(nullable = false, unique = true)
    private String roleName;

    private String description;

}

