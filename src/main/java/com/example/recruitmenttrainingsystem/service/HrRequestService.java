// src/main/java/com/example/recruitmenttrainingsystem/service/HrRequestService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HrRequestService {

    private final HrRequestRepository hrRequestRepository;
    private final TechnologyRepository technologyRepository;
    private final QuantityCandidateRepository quantityCandidateRepository;
    private final UserRepository userRepository;

    // GIỮ NGUYÊN: LẤY DANH SÁCH YÊU CẦU
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestRepository.findAll().stream()
                .map(hr -> new HrRequestResponse(
                        hr.getRequestId(),
                        hr.getRequestTitle(),
                        hr.getStatus(),
                        hr.getExpectedDeliveryDate(),
                        hr.getCreatedAt(),
                        hr.getNote(),
                        hr.getCreatedBy().getFullName(),
                        // MỚI: TRẢ VỀ techQuantities
                        hr.getQuantityCandidates().stream()
                                .map(qc -> new TechQuantityDto(qc.getTechnology().getId(), qc.getSoLuong()))
                                .toList()
                ))
                .toList();
    }

    // GIỮ NGUYÊN: TẠO YÊU CẦU
    public ResponseEntity<?> createHrRequest(CreateHrRequestDto dto) {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate minDate = LocalDate.now().plusMonths(2);
        if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
        }

        HrRequest request = new HrRequest();
        request.setRequestTitle(dto.getRequestTitle());
        request.setStatus("DANG_CHO");
        request.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        request.setNote(dto.getNote());
        request.setCreatedBy(user);

        HrRequest savedRequest = hrRequestRepository.save(request);

        for (var tq : dto.getTechQuantities()) {
            Technology tech = technologyRepository.findById(tq.getTechnologyId())
                    .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: ID = " + tq.getTechnologyId()));
            QuantityCandidate qc = new QuantityCandidate();
            qc.setHrRequest(savedRequest);
            qc.setTechnology(tech);
            qc.setSoLuong(tq.getSoLuong());
            quantityCandidateRepository.save(qc);
        }

        return ResponseEntity.ok(Map.of("message", "Yêu cầu nhân sự đã được tạo thành công!"));
    }

    // GIỮ NGUYÊN: LẤY DANH SÁCH CÔNG NGHỆ
    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }

    // MỚI: CẬP NHẬT YÊU CẦU
    @Transactional
    public ResponseEntity<?> updateHrRequest(Long id, CreateHrRequestDto dto) {
        HrRequest request = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        if (!"DANG_CHO".equals(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ có thể sửa yêu cầu ở trạng thái 'Đang chờ'"));
        }

        LocalDate minDate = LocalDate.now().plusMonths(2);
        if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
        }

        request.setRequestTitle(dto.getRequestTitle());
        request.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        request.setNote(dto.getNote());

        // XÓA CŨ
        quantityCandidateRepository.deleteAll(request.getQuantityCandidates());

        // TẠO MỚI
        for (var tq : dto.getTechQuantities()) {
            Technology tech = technologyRepository.findById(tq.getTechnologyId())
                    .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: ID = " + tq.getTechnologyId()));
            QuantityCandidate qc = new QuantityCandidate();
            qc.setHrRequest(request);
            qc.setTechnology(tech);
            qc.setSoLuong(tq.getSoLuong());
            quantityCandidateRepository.save(qc);
        }

        hrRequestRepository.save(request);
        return ResponseEntity.ok(Map.of("message", "Yêu cầu nhân sự đã được cập nhật thành công!"));
    }
}