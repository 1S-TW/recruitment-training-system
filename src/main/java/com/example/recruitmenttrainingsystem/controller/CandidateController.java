// src/main/java/com/example/recruitmenttrainingsystem/controller/CandidateController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CandidateListDto;
import com.example.recruitmenttrainingsystem.entity.CandidateReview;   // 👈 THÊM
import com.example.recruitmenttrainingsystem.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.recruitmenttrainingsystem.dto.CreateCandidateDto;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public ResponseEntity<List<CandidateListDto>> getCandidates(
            @RequestParam(name = "planId", required = false) Long planId
    ) {
        return ResponseEntity.ok(candidateService.getCandidates(planId));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<CandidateReview>> getReviews(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(candidateService.getReviewsByCandidate(id));
    }
    @PostMapping("/create")
    public ResponseEntity<CandidateListDto> createCandidate(
            @Valid @RequestBody CreateCandidateDto dto
    ) {
        CandidateListDto newCandidate = candidateService.createCandidate(dto);
        return ResponseEntity.ok(newCandidate);
    }
}
