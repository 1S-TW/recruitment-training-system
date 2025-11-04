package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Nếu bạn muốn biết ai là trưởng bộ phận
    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "department")
    private List<User> users;
}
