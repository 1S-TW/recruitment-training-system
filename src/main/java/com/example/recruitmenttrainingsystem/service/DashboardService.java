package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.DashboardStatsDTO;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import com.example.recruitmenttrainingsystem.repository.SummaryResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InternRepository internRepo;
    private final SummaryResultRepository summaryRepo;

    public DashboardStatsDTO getStats(LocalDate start, LocalDate end) {

        // Nếu start/end trống => return toàn bộ
        boolean all = (start == null || end == null);

        LocalDateTime dtStart = all
                ? LocalDate.of(1900, 1, 1).atStartOfDay()
                : start.atStartOfDay();

        LocalDateTime dtEnd = all
                ? LocalDate.of(3000, 1, 1).atTime(23, 59, 59)
                : end.atTime(23, 59, 59);

        // 1. Nhập học
        long totalEnroll = all
                ? internRepo.count()
                : internRepo.countByStartDateBetween(start, end);

        // 2. Tốt nghiệp (PASS)
        long totalGraduate = summaryRepo.countByInternshipResultIgnoreCaseAndUpdatedAtBetween(
                "Đạt", dtStart, dtEnd
        );

        // 3. Fail
        long totalFail = summaryRepo.countByInternshipResultIgnoreCaseAndUpdatedAtBetween(
                "Không đạt", dtStart, dtEnd
        );

        // 4. Pass/Fail Rate
        double passFailRate = (totalGraduate + totalFail == 0)
                ? 0
                : ((double) totalGraduate / (totalGraduate + totalFail)) * 100;

        // Format thành chuỗi với 2 chữ số thập phân và ký hiệu %
        String passFailRateStr = String.format("%.2f%%", passFailRate);


        // 5. Nghỉ thực tập
        long totalQuit = all
                ? internRepo.countByInternStatusIgnoreCase("Đã dừng thực tập")
                : internRepo.countByInternStatusIgnoreCaseAndEndDateBetween(
                "Đã dừng thực tập", start, end);


        // 6. Điểm tốt nghiệp trung bình
        Double avgScore = summaryRepo.avgFinalScore(dtStart, dtEnd);
        if (avgScore == null) avgScore = 0.0;

        return DashboardStatsDTO.builder()
                .totalEnroll(totalEnroll)
                .totalGraduate(totalGraduate)
                .totalFail(totalFail)
                .passFailRateStr(passFailRateStr)
                .totalQuit(totalQuit)
                .averageFinalScore(avgScore)
                .build();
    }
}
