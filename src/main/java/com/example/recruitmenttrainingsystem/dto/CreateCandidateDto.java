package com.example.recruitmenttrainingsystem.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CreateCandidateDto {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải là 10 chữ số và bắt đầu bằng số 0")
    private String phoneNumber;

    private String cvLink;

    @NotNull(message = "Thời gian hẹn phỏng vấn không được để trống")
    @Future(message = "Thời gian hẹn phỏng vấn phải ở tương lai (lớn hơn ngày hiện tại)")
    private LocalDateTime interviewDate;

    @NotNull(message = "Kế hoạch tuyển dụng không được để trống")
    private Long planId;
}