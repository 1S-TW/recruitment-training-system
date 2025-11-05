package com.example.recruitmenttrainingsystem.dto;

import lombok.Data;

@Data
public class UpdateRoleRequest {
    private String email;
    private String roleName;
}