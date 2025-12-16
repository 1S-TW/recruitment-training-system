package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.DashboardStatsDTO;
import com.example.recruitmenttrainingsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardStatsDTO getDashboardStats(
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,

            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return dashboardService.getStats(start, end);
    }

}
