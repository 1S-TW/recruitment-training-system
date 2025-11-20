// src/main/java/com/example/recruitmenttrainingsystem/repository/InternRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Intern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternRepository extends JpaRepository<Intern, Long> {

    boolean existsByCandidate_CandidateId(Long candidateId);

    List<Intern> findByInternStatusIgnoreCase(String internStatus);
}
