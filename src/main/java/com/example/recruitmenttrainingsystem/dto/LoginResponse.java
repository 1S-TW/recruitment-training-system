package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
    private UUID id;
}