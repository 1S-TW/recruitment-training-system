package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Data
@Table(name = "Department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users;
}