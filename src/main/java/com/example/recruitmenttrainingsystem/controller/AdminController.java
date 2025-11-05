package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.UpdateRoleRequest;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.service.RoleService;
import com.example.recruitmenttrainingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @PostMapping("/update-role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateRole(@RequestBody UpdateRoleRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            user.setRole(roleService.findByRoleName(request.getRoleName()).orElseThrow());
            userService.save(user);
            return ResponseEntity.ok("Cập nhật vai trò thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật vai trò");
        }
    }
}