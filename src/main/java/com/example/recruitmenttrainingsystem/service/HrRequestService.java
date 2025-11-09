package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestRepository.findAll().stream()
                .map(hr -> new HrRequestResponse(
                        hr.getRequestId(),
                        hr.getRequestTitle(),
                        hr.getStatus(),                    // String
                        hr.getExpectedDeliveryDate(),
                        hr.getCreatedAt(),
                        hr.getNote(),
                        hr.getCreatedBy().getFullName()
                ))
                .toList();
    }

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
        
        // SỬA: Dùng String thay vì enum
        request.setStatus("DANG_CHO");  // GIỮ NGUYÊN GIÁ TRỊ CŨ

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

    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }

    // ✅ Phê duyệt yêu cầu → APPROVED
    public HrRequestResponse approveRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        hrRequest.setStatus("APPROVED");
        hrRequest.setNote(note);
        hrRequestRepository.save(hrRequest);

        return new HrRequestResponse(
                hrRequest.getRequestId(),
                hrRequest.getRequestTitle(),
                hrRequest.getStatus(),
                hrRequest.getExpectedDeliveryDate(),
                hrRequest.getCreatedAt(),
                hrRequest.getNote(),
                hrRequest.getCreatedBy().getFullName()
        );
    }

    // ❌ Từ chối yêu cầu → CANCELED
    public HrRequestResponse rejectRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        hrRequest.setStatus("CANCELED");
        hrRequest.setNote(note);
        hrRequestRepository.save(hrRequest);

        return new HrRequestResponse(
                hrRequest.getRequestId(),
                hrRequest.getRequestTitle(),
                hrRequest.getStatus(),
                hrRequest.getExpectedDeliveryDate(),
                hrRequest.getCreatedAt(),
                hrRequest.getNote(),
                hrRequest.getCreatedBy().getFullName()
        );
    }
}