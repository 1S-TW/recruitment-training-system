package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt"
    )

    private String password;
}