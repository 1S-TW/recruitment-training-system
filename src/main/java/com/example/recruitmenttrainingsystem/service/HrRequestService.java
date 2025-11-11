package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.dto.PlanDefaultsDto;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
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
                    // Gọi 1 lần, reuse
                    List<QuantityCandidate> qcs = quantityCandidateRepository.findByHrRequest(hr);
                    int totalQuantity = qcs.stream().mapToInt(QuantityCandidate::getSoLuong).sum();

                    HrRequestResponse dto = new HrRequestResponse(
                            hr.getRequestId(),
                            hr.getRequestTitle(),
                            hr.getStatus(),
                            hr.getExpectedDeliveryDate(),
                            hr.getCreatedAt(),
                            hr.getNote(),
                            hr.getCreatedBy() != null ? hr.getCreatedBy().getFullName() : null,
                            totalQuantity
                    );

                    List<Map<String, Object>> techQuantities = qcs.stream()
                            .map(qc -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("technologyId", qc.getTechnology().getId());
                                map.put("technology", qc.getTechnology().getName());
                                map.put("quantity", qc.getSoLuong());
                                return map;
                            })
                            .toList();

                    dto.setTechQuantities(techQuantities);
                    return dto;
                })
                .toList();
    }

    @Transactional
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

    @Transactional
    public HrRequestResponse approveRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        String current = hrRequest.getStatus();
        if ("APPROVED".equalsIgnoreCase(current)) {
            // idempotent: giữ nguyên, trả về response hiện tại
        } else if ("CANCELED".equalsIgnoreCase(current)) {
            throw new IllegalStateException("Yêu cầu đã bị từ chối, không thể phê duyệt");
        } else if ("DANG_CHO".equalsIgnoreCase(current)) {
            hrRequest.setStatus("APPROVED");
            hrRequest.setNote(note);
            hrRequestRepository.save(hrRequest);
        } else {
            // nếu có trạng thái khác, tuỳ nghiệp vụ: reject hoặc cho phép
            hrRequest.setStatus("APPROVED");
            hrRequest.setNote(note);
            hrRequestRepository.save(hrRequest);
        }

        return buildHrRequestResponse(hrRequest);
    }

    @Transactional
    public HrRequestResponse rejectRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        String current = hrRequest.getStatus();
        if ("APPROVED".equalsIgnoreCase(current)) {
            throw new IllegalStateException("Yêu cầu đã được phê duyệt, không thể từ chối");
        }
        hrRequest.setStatus("CANCELED");
        hrRequest.setNote(note);
        hrRequestRepository.save(hrRequest);

        return buildHrRequestResponse(hrRequest);
    }

    // === Defaults cho modal "Thêm Plan" khi có requestId ===
    public PlanDefaultsDto buildPlanDefaultsFromRequest(Long requestId) {
        HrRequest req = hrRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + requestId));

        LocalDate deliveryDeadline = req.getExpectedDeliveryDate();
        // gợi ý: tuyển dụng trước bàn giao 7 ngày (anh có thể đổi quy tắc)
        LocalDate recruitmentDeadline = deliveryDeadline.minusDays(7);

        List<QuantityCandidate> qcs = quantityCandidateRepository.findByHrRequest_RequestId(requestId);
        int total = qcs.stream().mapToInt(QuantityCandidate::getSoLuong).sum();

        var techDetails = qcs.stream().map(qc -> {
            PlanDefaultsDto.TechQuantityDetail d = new PlanDefaultsDto.TechQuantityDetail();
            d.setTechnologyId(qc.getTechnology().getId());
            d.setTechnologyName(qc.getTechnology().getName());
            d.setSoLuong(qc.getSoLuong());
            return d;
        }).toList();

        PlanDefaultsDto dto = new PlanDefaultsDto();
        dto.setRequestId(requestId);
        dto.setSuggestedPlanName(req.getRequestTitle());
        dto.setStatus("DRAFT");
        dto.setRecruitmentDeadline(recruitmentDeadline);
        dto.setDeliveryDeadline(deliveryDeadline);
        dto.setNote(req.getNote());
        dto.setTotalCandidates(total);
        dto.setTechQuantities(techDetails);
        return dto;
    }

    private HrRequestResponse buildHrRequestResponse(HrRequest hrRequest) {
        List<QuantityCandidate> qcs = quantityCandidateRepository.findByHrRequest(hrRequest);
        int totalQuantity = qcs.stream().mapToInt(QuantityCandidate::getSoLuong).sum();

        HrRequestResponse dto = new HrRequestResponse(
                hrRequest.getRequestId(),
                hrRequest.getRequestTitle(),
                hrRequest.getStatus(),
                hrRequest.getExpectedDeliveryDate(),
                hrRequest.getCreatedAt(),
                hrRequest.getNote(),
                hrRequest.getCreatedBy() != null ? hrRequest.getCreatedBy().getFullName() : null,
                totalQuantity
        );

        List<Map<String, Object>> techQuantities = qcs.stream()
                .map(qc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("technologyId", qc.getTechnology().getId());
                    map.put("technology", qc.getTechnology().getName());
                    map.put("quantity", qc.getSoLuong());
                    return map;
                })
                .toList();

        dto.setTechQuantities(techQuantities);
        return dto;
    }

    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }
}
