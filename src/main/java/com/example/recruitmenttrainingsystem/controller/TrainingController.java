// src/main/java/com/example/recruitmenttrainingsystem/controller/TrainingController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.TrainingDto;
import com.example.recruitmenttrainingsystem.dto.TrainingScoreDto;
import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.service.TrainingService;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final InternRepository internRepository;

    // GET: Lấy toàn bộ thực tập sinh (danh sách đào tạo)
    @GetMapping
    public ResponseEntity<List<TrainingDto>> getAll() {
        return ResponseEntity.ok(trainingService.getAll());
    }

    // GET: Lấy danh sách TTS theo kế hoạch tuyển dụng
    // Ví dụ: /api/trainings/by-plan?planId=5
    @GetMapping("/by-plan")
    public ResponseEntity<List<TrainingDto>> getByPlan(@RequestParam("planId") Long planId) {
        List<TrainingDto> result = trainingService.getByPlan(planId);
        return ResponseEntity.ok(result);
    }

    // PUT: Cập nhật điểm số + kết quả thực tập (PASS/FAIL) + lý do khi <7
    // Body: TrainingScoreDto (có thể chứa reason trong từng CourseScoreDto nếu <7)
    @PutMapping("/{internId}/scores")
    public ResponseEntity<TrainingDto> updateScores(
            @PathVariable Long internId,
            @Valid @RequestBody TrainingScoreDto dto) {

        TrainingDto updated = trainingService.updateScores(internId, dto);
        return ResponseEntity.ok(updated);
    }

    // PUT: Dừng thực tập (khi TTS bỏ học, không tiếp tục, v.v.)
    @PutMapping("/{internId}/stop")
    public ResponseEntity<TrainingDto> stopInternship(@PathVariable Long internId) {
        TrainingDto result = trainingService.stopInternship(internId);
        return ResponseEntity.ok(result);
    }

    // GET: Đếm tổng số TTS đang tham gia đào tạo theo kế hoạch
    @GetMapping("/count-by-plan")
    public ResponseEntity<Long> countInternsByPlan(@RequestParam("planId") Long planId) {
        long count = internRepository.countByRecruitmentPlan_RecruitmentPlanId(planId);
        return ResponseEntity.ok(count);
    }

    // GET: Đếm số TTS đã PASS & bàn giao thành công theo kế hoạch
    @GetMapping("/delivered-count-by-plan")
    public ResponseEntity<Long> countDeliveredByPlan(@RequestParam("planId") Long planId) {
        long count = trainingService.countInternsDeliveredByPlan(planId);
        return ResponseEntity.ok(count);
    }

    // NEW: Lấy chi tiết 1 thực tập sinh (dùng để xem form chấm điểm chi tiết)
    @GetMapping("/{internId}")
    public ResponseEntity<TrainingDto> getById(@PathVariable Long internId) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực tập sinh ID: " + internId));
        return ResponseEntity.ok(trainingService.toTrainingDto(intern));
    }
}