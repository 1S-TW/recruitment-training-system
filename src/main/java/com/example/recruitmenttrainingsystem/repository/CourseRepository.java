package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


    // CourseRepository.java
public interface CourseRepository extends JpaRepository<Course, Long> {
        List<Course> findAllByOrderByCourseId(); // hoặc findAll()
        Optional<Course> findByCourseName(String courseName);
}


