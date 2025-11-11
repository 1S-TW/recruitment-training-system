package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import com.example.recruitmenttrainingsystem.repository.HrRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification; // <-- IMPORT MỚI
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class HrRequestService {

    private final HrRequestRepository hrRequestRepository;

    // --- HÀM CŨ CỦA BẠN (Giữ nguyên) ---
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestRepository.findAll()
                .stream()
                .map(hr -> new HrRequestResponse(
                        hr.getRequestId(),
                        hr.getRequestTitle(),
                        hr.getStatus(),
                        hr.getExpectedDeliveryDate(),
                        hr.getCreatedAt(),
                        hr.getNote(),
                        hr.getCreatedBy().getFullName() // chỉ lấy tên người tạo
                ))
                .toList();
    }


    // ==============================================================
    // === BẮT ĐẦU PHẦN CODE MỚI ĐƯỢC THÊM VÀO ===
    // ==============================================================

    /**
     * Phương thức tìm kiếm động theo tiêu đề (title) và/hoặc trạng thái (status).
     * @param title Tiêu đề yêu cầu (tìm kiếm tương đối - LIKE)
     * @param status Trạng thái yêu cầu (tìm kiếm chính xác - EQUAL)
     * @return Danh sách các HrRequestResponse phù hợp
     */
    public List<HrRequestResponse> searchHrRequests(String title, String status) {

        // 1. Bắt đầu với một Specification cơ sở (không có điều kiện gì)
        Specification<HrRequest> spec = Specification.where(null);

        // 2. Thêm điều kiện tìm kiếm theo 'requestTitle' nếu title được cung cấp
        if (title != null && !title.isEmpty()) {
            spec = spec.and(hasTitleLike(title));
        }

        // 3. Thêm điều kiện tìm kiếm theo 'status' nếu status được cung cấp
        if (status != null && !status.isEmpty()) {
            spec = spec.and(hasStatus(status));
        }

        // 4. Thực thi truy vấn và map kết quả
        return hrRequestRepository.findAll(spec)
                .stream()
                .map(this::mapEntityToResponse) // Dùng hàm helper private bên dưới
                .toList();
    }

    /**
     * Trả về một Specification để lọc theo 'requestTitle' (sử dụng LIKE).
     */
    private Specification<HrRequest> hasTitleLike(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("requestTitle"), "%" + title + "%");
    }

    /**
     * Trả về một Specification để lọc theo 'status' (sử dụng EQUAL).
     */
    private Specification<HrRequest> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Phương thức private helper mới để chuyển đổi Entity sang DTO.
     */
    private HrRequestResponse mapEntityToResponse(HrRequest hr) {
        return new HrRequestResponse(
                hr.getRequestId(),
                hr.getRequestTitle(),
                hr.getStatus(),
                hr.getExpectedDeliveryDate(),
                hr.getCreatedAt(),
                hr.getNote(),
                hr.getCreatedBy().getFullName()
        );
    }

    // ==============================================================
    // === KẾT THÚC PHẦN CODE MỚI ===
    // ==============================================================
}