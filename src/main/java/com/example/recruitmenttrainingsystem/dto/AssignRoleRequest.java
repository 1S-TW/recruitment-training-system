// Tên file: dto/AssignRoleRequest.java
package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {
    private String roleName;
    private Boolean status;
}