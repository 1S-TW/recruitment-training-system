package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.service.HrRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hr-request")
public class HrRequestController {

    private final HrRequestService hrRequestService;

    @GetMapping
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestService.getAllHrRequests();
    }

    // ✅ API phê duyệt
    @PutMapping("/{id}/approve")
    public HrRequestResponse approveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String note
    ) {
        return hrRequestService.approveRequest(id, note);
    }

    // ❌ API từ chối
    @PutMapping("/{id}/reject")
    public HrRequestResponse rejectRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String note
    ) {
        return hrRequestService.rejectRequest(id, note);
    }
}
