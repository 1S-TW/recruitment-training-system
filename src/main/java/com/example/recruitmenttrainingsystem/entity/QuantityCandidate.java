package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quantity_candidate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuantityCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private HrRequest hrRequest;  // DÙNG OBJECT, KHÔNG DÙNG requestId

    @ManyToOne
    @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology; // DÙNG OBJECT

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}