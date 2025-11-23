// src/main/java/com/example/recruitmenttrainingsystem/controller/HrRequestController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.dto.PlanDefaultsDto;
import com.example.recruitmenttrainingsystem.dto.RejectHrRequestDto;
import com.example.recruitmenttrainingsystem.entity.Technology;
import com.example.recruitmenttrainingsystem.service.HrRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hr-request")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class HrRequestController {

    private final HrRequestService hrRequestService;

    // ========== QUERY ==========

    @GetMapping
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestService.getAllHrRequests();
    }

    @GetMapping("/{id}")
    public HrRequestResponse getById(@PathVariable Long id) {
        return hrRequestService.getById(id);
    }

    // ========== CREATE / UPDATE ==========

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CreateHrRequestDto dto) {
        return hrRequestService.createHrRequest(dto);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CreateHrRequestDto dto) {
        return hrRequestService.updateHrRequest(id, dto);
    }

    // ========== APPROVE / REJECT ==========

    // ghi chú phê duyệt: truyền qua query param ?note=...
    @PutMapping("/{id}/approve")
    public HrRequestResponse approveRequest(@PathVariable Long id,
                                            @RequestParam(required = false) String note) {
        return hrRequestService.approveRequest(id, note);
    }

    // lý do từ chối: truyền trong body JSON { "rejectionReason": "..." }
    @PutMapping("/{id}/reject")
    public HrRequestResponse rejectRequest(@PathVariable Long id,
                                           @Valid @RequestBody RejectHrRequestDto dto) {
        return hrRequestService.rejectRequest(id, dto.getRejectionReason());
    }

    // ========== OTHERS ==========

    @GetMapping("/technologies")
    public List<Technology> getTechnologies() {
        return hrRequestService.getTechnologies();
    }

    @GetMapping("/{id}/plan-defaults")
    public ResponseEntity<PlanDefaultsDto> getPlanDefaults(@PathVariable Long id) {
        return ResponseEntity.ok(hrRequestService.buildPlanDefaultsFromRequest(id));
    }
}