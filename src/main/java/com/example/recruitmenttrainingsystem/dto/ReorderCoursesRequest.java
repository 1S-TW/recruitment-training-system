// src/main/java/com/example/recruitmenttrainingsystem/dto/ReorderCoursesRequest.java
package com.example.recruitmenttrainingsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReorderCoursesRequest {
    private List<Long> courseIds;
}
