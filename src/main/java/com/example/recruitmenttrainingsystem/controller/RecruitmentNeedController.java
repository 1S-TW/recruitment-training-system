package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.RecruitmentNeedDto;
import com.example.recruitmenttrainingsystem.entity.RecruitmentNeedStatus;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.UserRepository; // Import UserRepository của bạn
import com.example.recruitmenttrainingsystem.service.RecruitmentNeedService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruitment-needs")
public class RecruitmentNeedController {

    @Autowired
    private RecruitmentNeedService recruitmentNeedService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<Page<RecruitmentNeedDto>> searchRecruitmentNeeds(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RecruitmentNeedStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Authentication authentication) {

        // 1. Lấy email (là 'username' trong Spring Security)
        String currentUserEmail = authentication.getName();

        // 2. Sửa lỗi: Thay 'findByUsername' bằng 'findByEmail'
        User pmUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + currentUserEmail));

        // 3. Gọi service
        Page<RecruitmentNeedDto> results = recruitmentNeedService.searchNeeds(name, status, pmUser, pageable);

        return ResponseEntity.ok(results);
    }
}