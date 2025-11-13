package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

// (+) thêm import
import com.fasterxml.jackson.annotation.JsonIgnore;      // (+)
import lombok.EqualsAndHashCode;                        // (+)
import lombok.ToString;                                 // (+)

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
    @ToString.Exclude                   // (+)
    @EqualsAndHashCode.Exclude          // (+)
    @JsonIgnore                         // (+) tránh vòng lặp HrRequest -> list -> item -> HrRequest...
    private HrRequest hrRequest;

    @ManyToOne
    @JoinColumn(name = "technology_id", nullable = false)
    @ToString.Exclude                   // (+)
    @EqualsAndHashCode.Exclude          // (+)
    @JsonIgnore                         // (+) không serialize nguyên entity Technology khi không cần
    private Technology technology;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}
