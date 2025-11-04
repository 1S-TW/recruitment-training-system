package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    private Long id; // 1..5

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
