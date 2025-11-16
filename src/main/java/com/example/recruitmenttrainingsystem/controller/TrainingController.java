// package com.example.recruitmenttrainingsystem.controller;

// import com.example.recruitmenttrainingsystem.dto.TrainingDto;
// import com.example.recruitmenttrainingsystem.service.TrainingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:5173") // giống các controller khác
// public class TrainingController {

//     private final TrainingService trainingService;

//     // 👉 endpoint mà FE đang gọi: api.get("/trainings")
//     @GetMapping("/trainings")
//     public ResponseEntity<List<TrainingDto>> getTrainings() {
//         List<TrainingDto> list = trainingService.getTrainingList();
//         return ResponseEntity.ok(list);
//     }
// }
