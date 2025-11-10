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
                .map(hr -> {
                    int totalQuantity = quantityCandidateRepository
                            .findByHrRequest(hr)
                            .stream()
                            .mapToInt(QuantityCandidate::getSoLuong)
                            .sum();

                    HrRequestResponse dto = new HrRequestResponse(
                            hr.getRequestId(),
                            hr.getRequestTitle(),
                            hr.getStatus(),
                            hr.getExpectedDeliveryDate(),
                            hr.getCreatedAt(),
                            hr.getNote(),
                            hr.getCreatedBy().getFullName(),
                            totalQuantity
                    );

                    List<String> techNames = quantityCandidateRepository.findByHrRequest(hr)
                            .stream()
                            .map(qc -> qc.getTechnology().getName()) // ✅ Sửa tại đây
                            .distinct()
                            .toList();

                    dto.setTechnologies(techNames);
                    return dto;
                })
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

    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }

    public HrRequestResponse approveRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        hrRequest.setStatus("APPROVED");
        hrRequest.setNote(note);
        hrRequestRepository.save(hrRequest);

        int totalQuantity = quantityCandidateRepository
                .findByHrRequest(hrRequest)
                .stream()
                .mapToInt(QuantityCandidate::getSoLuong)
                .sum();

        HrRequestResponse dto = new HrRequestResponse(
                hrRequest.getRequestId(),
                hrRequest.getRequestTitle(),
                hrRequest.getStatus(),
                hrRequest.getExpectedDeliveryDate(),
                hrRequest.getCreatedAt(),
                hrRequest.getNote(),
                hrRequest.getCreatedBy().getFullName(),
                totalQuantity
        );

        List<String> techNames = quantityCandidateRepository.findByHrRequest(hrRequest)
                .stream()
                .map(qc -> qc.getTechnology().getName()) // ✅ Sửa tại đây
                .distinct()
                .toList();

        dto.setTechnologies(techNames);
        return dto;
    }

    public HrRequestResponse rejectRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        hrRequest.setStatus("CANCELED");
        hrRequest.setNote(note);
        hrRequestRepository.save(hrRequest);

        int totalQuantity = quantityCandidateRepository
                .findByHrRequest(hrRequest)
                .stream()
                .mapToInt(QuantityCandidate::getSoLuong)
                .sum();

        HrRequestResponse dto = new HrRequestResponse(
                hrRequest.getRequestId(),
                hrRequest.getRequestTitle(),
                hrRequest.getStatus(),
                hrRequest.getExpectedDeliveryDate(),
                hrRequest.getCreatedAt(),
                hrRequest.getNote(),
                hrRequest.getCreatedBy().getFullName(),
                totalQuantity
        );

        List<String> techNames = quantityCandidateRepository.findByHrRequest(hrRequest)
                .stream()
                .map(qc -> qc.getTechnology().getName()) // ✅ Sửa tại đây
                .distinct()
                .toList();

        dto.setTechnologies(techNames);
        return dto;
    }
}