package com.example.recruitmenttrainingsystem.dto;

import lombok.*;

@Getter @Setter @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
}
