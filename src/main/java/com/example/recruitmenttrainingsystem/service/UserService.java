package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DepartmentService departmentService;

    public User save(User user) {
        if (user.getRole() == null) {
            user.setRole(roleService.findByRoleName("HR").orElseThrow(() -> new RuntimeException("Role HR not found")));
        }
        if (user.getDepartment() == null) {
            user.setDepartment(departmentService.findByDepartmentName("Default Department")
                    .orElseThrow(() -> new RuntimeException("Default Department not found")));
        }
        user.setStatus(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
    }
}