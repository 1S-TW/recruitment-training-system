package com.example.recruitmenttrainingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDto {

    private Long trainingId;        // map từ candidateId
    private String traineeName;     // tên ứng viên

    private LocalDate startDate;    // ngày bắt đầu thực tập (nếu sau này có)
    private Integer trainingDays;   // số ngày TT (nếu có)

    private Integer subject1Score;  // Môn học 1 (tạm để trống / map sau)
    private Integer subject2Score;  // Môn học 2
    private Integer subject3Score;  // Môn học 3

    private Integer finalScore;     // Tổng kết (VD: testScore hoặc điểm tổng)
    private String teamEvaluation;  // Đánh giá trên team

    // dùng cho filter "Trạng thái thực tập..."
    private String internStatus;    // ví dụ: "Đang thực tập", "Đã kết thúc"...
}
