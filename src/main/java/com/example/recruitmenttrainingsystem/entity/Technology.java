package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technology", uniqueConstraints = @UniqueConstraint(columnNames = "technology_name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class    Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technology_id")
    private Long id;

    @Column(name = "technology_name", length = 100, nullable = false, unique = true)
    private String name;
}