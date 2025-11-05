package com.example.recruitmenttrainingsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer"; // default token type


    public LoginResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}