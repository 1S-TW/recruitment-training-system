// src/main/java/com/example/recruitmenttrainingsystem/controller/CourseController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CourseDto;
import com.example.recruitmenttrainingsystem.dto.ReorderCoursesRequest;   // ✅ THÊM IMPORT NÀY
import com.example.recruitmenttrainingsystem.entity.Course;
import com.example.recruitmenttrainingsystem.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CourseDto dto) {
        return ResponseEntity.ok(courseService.createCourse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CourseDto dto) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Xóa môn học thành công");
    }

    // ✅ API sắp xếp thứ tự môn học: nhận DTO bọc courseIds
    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody ReorderCoursesRequest request) {
        courseService.reorderCourses(request.getCourseIds());
        return ResponseEntity.ok("Đã cập nhật thứ tự môn học");
    }
}
