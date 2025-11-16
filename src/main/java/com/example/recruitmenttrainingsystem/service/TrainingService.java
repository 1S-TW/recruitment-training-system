// package com.example.recruitmenttrainingsystem.service;

// import com.example.recruitmenttrainingsystem.dto.TrainingDto;
// import com.example.recruitmenttrainingsystem.entity.Candidate;
// import com.example.recruitmenttrainingsystem.repository.CandidateRepository;

// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class TrainingService {

//     private final CandidateRepository candidateRepository;

//     /**
//      * Lấy danh sách đào tạo:
//      * finalResult = PASS
//      * status = Đã nhận việc
//      */
//     @Transactional(readOnly = true)
//     public List<TrainingDto> getTrainingList() {
//         List<Candidate> candidates =
//                 candidateRepository.findByFinalResultIgnoreCaseAndStatusIgnoreCase(
//                         "PASS",
//                         "Đã nhận việc"
//                 );

//         return candidates.stream()
//                 .map(this::mapCandidateToTraining)
//                 .collect(Collectors.toList());
//     }

//     /** Mapping từ Candidate → TrainingDto */
//     private TrainingDto mapCandidateToTraining(Candidate c) {

//         String NA = "NA";

//         return TrainingDto.builder()
//                 .trainingId(c.getCandidateId())
//                 .traineeName(c.getFullName())

//                 .startDate(null)
//                 .trainingDays(null)

//                 .subject1Score(NA)
//                 .subject2Score(NA)
//                 .subject3Score(NA)
//                 .finalScore(NA)
//                 .teamEvaluation(NA)

//                 .internStatus("Đang thực tập")
//                 .build();
//     }
// }
