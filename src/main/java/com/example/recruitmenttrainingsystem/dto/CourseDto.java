    package com.example.recruitmenttrainingsystem.dto;

    import lombok.Data;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Min;

    @Data
    public class CourseDto {
        private Long courseId;

        @NotBlank(message = "Tên môn học không được để trống")
        private String courseName;

        private String description;

        @NotNull(message = "Số ngày học là bắt buộc")
        @Min(value = 1, message = "Số ngày học phải lớn hơn 0")
        private Integer durationDays;
    }