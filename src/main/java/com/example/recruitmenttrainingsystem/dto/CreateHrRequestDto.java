package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateHrRequestDto {

    @NotBlank(message = "Tên nhu cầu không được để trống")
    private String requestTitle;

    @NotNull(message = "Thời hạn bàn giao là bắt buộc")
    private LocalDate expectedDeliveryDate;

    private String note;

    @NotEmpty(message = "Phải chọn ít nhất 1 công nghệ")
    private List<TechQuantity> techQuantities;

    // Getters & Setters
    public String getRequestTitle() { return requestTitle; }
    public void setRequestTitle(String requestTitle) { this.requestTitle = requestTitle; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<TechQuantity> getTechQuantities() { return techQuantities; }
    public void setTechQuantities(List<TechQuantity> techQuantities) { this.techQuantities = techQuantities; }

    // --- NỘI CLASS TECHQUANTITY ---
    public static class TechQuantity {
        @NotNull(message = "ID công nghệ không được null")
        private Long technologyId;  // Long

        @NotNull(message = "Số lượng không được null")
        private Integer soLuong;    // Integer

        // Getters & Setters
        public Long getTechnologyId() { return technologyId; }
        public void setTechnologyId(Long technologyId) { this.technologyId = technologyId; }

        public Integer getSoLuong() { return soLuong; }
        public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    }
}