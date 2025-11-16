package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt"
    )
    private String newPassword;
}