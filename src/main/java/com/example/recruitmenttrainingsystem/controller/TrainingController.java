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

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final InternRepository internRepository;

    @GetMapping
    public List<TrainingDto> getAll() {
        return trainingService.getAll();
    }

    // ⭐ NEW: Lấy danh sách TTS theo kế hoạch tuyển dụng
    // Ví dụ: GET /api/trainings/by-plan?planId=5
    @GetMapping("/by-plan")
    public ResponseEntity<List<TrainingDto>> getByPlan(@RequestParam("planId") Long planId) {
        return ResponseEntity.ok(trainingService.getByPlan(planId));
    }

    @PutMapping("/{internId}/scores")
    public ResponseEntity<TrainingDto> updateScores(@PathVariable Long internId,
                                                    @RequestBody TrainingScoreDto dto) {
        TrainingDto result = trainingService.updateScores(internId, dto);
        return ResponseEntity.ok(result);
    }

    // === ENDPOINT DỪNG THỰC TẬP - CHẠY NGON 100% ===
    // src/main/java/com/example/recruitmenttrainingsystem/controller/TrainingController.java

    @PutMapping("/{internId}/stop")
    public ResponseEntity<TrainingDto> stopInternship(@PathVariable Long internId) {
        System.out.println(">>> ĐÃ VÀO ENDPOINT /stop - internId = " + internId);

        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thực tập sinh ID: " + internId));

        // set trạng thái & ngày kết thúc thực tập
        intern.setInternStatus("Đã dừng thực tập");
        intern.setEndDate(java.time.LocalDate.now()); // 👈 ngày dừng thực tập

        internRepository.save(intern);

        // Sau khi dừng thực tập, kiểm tra xem kế hoạch/nhu cầu đã kết thúc chưa
        trainingService.checkRequestAndPlanStatusByInternId(internId);

        return ResponseEntity.ok(trainingService.toTrainingDto(intern));
    }


    // === ĐẾM SỐ LƯỢNG TTS THAM GIA ĐÀO TẠO THEO KẾ HOẠCH ===
    // Ví dụ: GET /api/trainings/count-by-plan?planId=5  -> 1, 2, 3, ...
    @GetMapping("/count-by-plan")
    public ResponseEntity<Long> countInternsByPlan(@RequestParam("planId") Long planId) {
        long count = internRepository.countByRecruitmentPlan_RecruitmentPlanId(planId);
        return ResponseEntity.ok(count);
    }

    // === NEW: ĐẾM SỐ LƯỢNG TTS ĐÃ BÀN GIAO (PASS & ĐÃ HOÀN THÀNH) THEO KẾ HOẠCH ===
    // Ví dụ: GET /api/trainings/delivered-count-by-plan?planId=5  -> 0, 1, 2, ...
    @GetMapping("/delivered-count-by-plan")
    public ResponseEntity<Long> countDeliveredByPlan(@RequestParam("planId") Long planId) {
        long count = trainingService.countInternsDeliveredByPlan(planId);
        return ResponseEntity.ok(count);
    }
}
