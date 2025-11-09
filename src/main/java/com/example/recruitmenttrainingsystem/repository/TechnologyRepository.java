package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
}