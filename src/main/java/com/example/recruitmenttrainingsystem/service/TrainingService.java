// src/main/java/com/example/recruitmenttrainingsystem/service/TrainingService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.TrainingDto;
import com.example.recruitmenttrainingsystem.entity.Candidate;
import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final InternRepository internRepository;

    /**
     * Lấy danh sách THỰC TẬP SINH từ bảng intern.
     * FE sẽ lọc Đang thực tập / Đã kết thúc...
     */
    public List<TrainingDto> getTrainings() {

        List<Intern> interns = internRepository.findAll();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        return interns.stream()
                .map(intern -> {
                    Candidate c = intern.getCandidate();
                    LocalDate startDate = intern.getStartDate();

                    Long trainingDays = null;
                    if (startDate != null) {
                        trainingDays = ChronoUnit.DAYS.between(startDate, today);
                    }

                    return TrainingDto.builder()
                            .internId(intern.getInternId())
                            .candidateId(c != null ? c.getCandidateId() : null)
                            .fullName(c != null ? c.getFullName() : null)
                            .startDate(startDate)
                            .trainingDays(trainingDays)
                            .subject1(null)
                            .subject2(null)
                            .subject3(null)
                            .summaryResult(null)
                            .teamReview(null)
                            .internStatus(intern.getInternStatus())
                            .build();
                })
                .toList();
    }
}
    