// Tên file: dto/AssignRoleRequest.java
package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {

    @NotBlank(message = "Tên role không được để trống")
    private String roleName;
}