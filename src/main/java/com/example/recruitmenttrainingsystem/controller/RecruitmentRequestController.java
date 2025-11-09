package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.entity.RecruitmentRequest;
import com.example.recruitmenttrainingsystem.entity.RecruitmentRequestStatus;
import com.example.recruitmenttrainingsystem.service.RecruitmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment-requests")
public class RecruitmentRequestController {

    @Autowired
    private RecruitmentRequestService recruitmentRequestService;

    @GetMapping("/search")
    public ResponseEntity<List<RecruitmentRequest>> search(
            @RequestParam(required = false) String requestName,
            @RequestParam(required = false) RecruitmentRequestStatus status) {
//lay du lieu
        List<RecruitmentRequest> results = recruitmentRequestService.searchRecruitmentRequests(requestName, status);
        return ResponseEntity.ok(results);
    }
}