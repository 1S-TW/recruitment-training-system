package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.RecruitmentRequest;
import com.example.recruitmenttrainingsystem.entity.RecruitmentRequestStatus;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.RecruitmentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
// import org.springframework.security.core.Authentication; // Tạm thời không dùng
// import org.springframework.security.core.context.SecurityContextHolder; // Tạm thời không dùng
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruitmentRequestService {

    @Autowired
    private RecruitmentRequestRepository recruitmentRequestRepository;

    public List<RecruitmentRequest> searchRecruitmentRequests(String requestName, RecruitmentRequestStatus status) {

        // --- BẮT ĐẦU VÔ HIỆU HÓA ĐỂ TEST ---
        // Lấy thông tin người dùng đang đăng nhập
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // User currentUser = (User) authentication.getPrincipal();
        // --- KẾT THÚC VÔ HIỆU HÓA ---

        Specification<RecruitmentRequest> spec = Specification.where(null);

        // --- BẮT ĐẦU VÔ HIỆU HÓA ĐỂ TEST ---
        // QUAN TRỌNG: Tạm thời tắt logic lọc theo người dùng
        // spec = spec.and((root, query, cb) -> cb.equal(root.get("projectManager"), currentUser));
        // --- KẾT THÚC VÔ HIỆU HÓA ---


        // Thêm điều kiện lọc theo tên (nếu có)
        if (requestName != null && !requestName.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("requestName"), "%" + requestName + "%"));
        }

        // Thêm điều kiện lọc theo trạng thái (nếu có)
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return recruitmentRequestRepository.findAll(spec);
    }
}