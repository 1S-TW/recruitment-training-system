package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.entity.Technology;
import com.example.recruitmenttrainingsystem.service.HrRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CreateHrRequestDto dto) {
        return hrRequestService.createHrRequest(dto);
    }

    @GetMapping("/technologies")
    public List<Technology> getTechnologies() {
        return hrRequestService.getTechnologies();
    }
}