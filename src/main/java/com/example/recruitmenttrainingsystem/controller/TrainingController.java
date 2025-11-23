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

    @PutMapping("/{internId}/scores")
    public ResponseEntity<TrainingDto> updateScores(@PathVariable Long internId,
                                                    @RequestBody TrainingScoreDto dto) {
        TrainingDto result = trainingService.updateScores(internId, dto);
        return ResponseEntity.ok(result);
    }

    // === ENDPOINT DỪNG THỰC TẬP - CHẠY NGON 100% ===
    @PutMapping("/{internId}/stop")
    public ResponseEntity<TrainingDto> stopInternship(@PathVariable Long internId) {
        System.out.println(">>> ĐÃ VÀO ENDPOINT /stop - internId = " + internId); // LOG ĐỂ CHECK

        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thực tập sinh ID: " + internId));

        intern.setInternStatus("Đã dừng thực tập");
        internRepository.save(intern);

        return ResponseEntity.ok(trainingService.toTrainingDto(intern));
    }
}
