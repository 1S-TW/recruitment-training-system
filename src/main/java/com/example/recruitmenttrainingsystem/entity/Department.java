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

    // 🔹 Nếu muốn biết ai là trưởng bộ phận (1 phòng chỉ có 1 trưởng)
    @OneToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    private User manager;

    // 🔹 Một phòng có nhiều nhân viên (user)
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
