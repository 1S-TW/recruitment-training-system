// src/main/java/com/example/recruitmenttrainingsystem/service/TrainingService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.TrainingDto;
import com.example.recruitmenttrainingsystem.entity.Candidate;
import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final InternRepository internRepository;

    public List<TrainingDto> getTrainings() {

        List<Intern> interns = internRepository.findAll();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        return interns.stream()
                .map(intern -> {
                    Candidate c = intern.getCandidate();
                    LocalDate startDate = intern.getStartDate();

                    Long trainingDays = null;
                    if (startDate != null) {
                        trainingDays = calculateWorkingDays(startDate, today); // đã trừ T7, CN
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
                            .subject4(null)   // ✅ mới
                            .subject5(null)   // ✅ mới
                            .subject6(null)   // ✅ mới
                            .summaryResult(null)
                            .teamReview(null)
                            .internStatus(intern.getInternStatus())
                            .build();
                })
                .toList();
    }

    /**
     * Tính số ngày làm việc (không tính Thứ 7 & Chủ nhật)
     * startDate: ngày bắt đầu
     * endDate: ngày kết thúc (ở đây đang là ngày hiện tại), giống logic DAYS.between: [start, end)
     */
    private long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        // nếu endDate trước startDate thì coi như 0 ngày
        if (endDate == null || startDate == null || !endDate.isAfter(startDate)) {
            return 0L;
        }

        long workingDays = 0L;
        LocalDate d = startDate;

        // giống ChronoUnit.DAYS.between(start, end): lặp tới ngày TRƯỚC endDate
        while (d.isBefore(endDate)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            d = d.plusDays(1);
        }

        return workingDays;
    }
}
