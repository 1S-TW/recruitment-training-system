package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.recruitmenttrainingsystem.service.HrRequestService;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import lombok.RequiredArgsConstructor;
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
}
