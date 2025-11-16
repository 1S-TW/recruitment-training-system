// src/main/java/com/example/recruitmenttrainingsystem/controller/CandidateController.java
package com.example.recruitmenttrainingsystem.controller;
import com.example.recruitmenttrainingsystem.dto.AddCandidateResultDto;
import com.example.recruitmenttrainingsystem.dto.CreateCandidateDto;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
// lisst candidate
    @GetMapping
    public ResponseEntity<List<CandidateListDto>> getCandidates(
            @RequestParam(name = "planId", required = false) Long planId
    ) {
        return ResponseEntity.ok(candidateService.getCandidates(planId));
    }
//review candidate
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<CandidateReview>> getReviews(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(candidateService.getReviewsByCandidate(id));
    }
    //add candidate
    @PostMapping("/create")
    public ResponseEntity<CandidateListDto> createCandidate(
            @Valid @RequestBody CreateCandidateDto dto
    ) {
        CandidateListDto newCandidate = candidateService.createCandidate(dto);
        return ResponseEntity.ok(newCandidate);
    }
    @PostMapping("/{id}/add-result")
    public ResponseEntity<CandidateListDto> addResult(
            @PathVariable("id") Long candidateId,
            @Valid @RequestBody AddCandidateResultDto dto,
            Authentication auth // Lấy thông tin người đang chấm điểm
    ) {
        // auth.getName() chính là email của user (reviewer)
        CandidateListDto updatedCandidate = candidateService.addCandidateResult(
                candidateId,
                dto,
                auth.getName()
        );
        return ResponseEntity.ok(updatedCandidate);
    }
}

