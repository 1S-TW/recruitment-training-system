// src/main/java/com/example/recruitmenttrainingsystem/service/HrRequestService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.dto.PlanDefaultsDto;
import com.example.recruitmenttrainingsystem.dto.TechQuantityDto;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import com.example.recruitmenttrainingsystem.entity.QuantityCandidate;
import com.example.recruitmenttrainingsystem.entity.Technology;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.HrRequestRepository;
import com.example.recruitmenttrainingsystem.repository.QuantityCandidateRepository;
import com.example.recruitmenttrainingsystem.repository.TechnologyRepository;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    // ===================== QUERY =====================

    @Transactional(readOnly = true)
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponseWithTechs)
                .toList();
    }

    @Transactional(readOnly = true)
    public HrRequestResponse getById(Long id) {
        HrRequest hr = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + id));
        return toResponseWithTechs(hr);
    }

    // ===================== CREATE / UPDATE =====================

    @Transactional
    public ResponseEntity<?> createHrRequest(CreateHrRequestDto dto) {
        // demo: lấy user id=1 (giữ theo bạn đang test)
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate minDate = LocalDate.now().plusMonths(2);
        if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
        }

        HrRequest req = new HrRequest();
        req.setRequestTitle(dto.getRequestTitle());
        req.setStatus("NEW");
        req.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        req.setNote(dto.getNote());
        req.setCreatedBy(user);

        HrRequest saved = hrRequestRepository.save(req);

        // insert quantities
        for (var tq : dto.getTechQuantities()) {
            Technology tech = technologyRepository.findById(tq.getTechnologyId())
                    .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: " + tq.getTechnologyId()));
            QuantityCandidate qc = new QuantityCandidate();
            qc.setHrRequest(saved);
            qc.setTechnology(tech);
            qc.setSoLuong(tq.getSoLuong());
            quantityCandidateRepository.save(qc);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Yêu cầu nhân sự đã được tạo thành công!");
        payload.put("requestId", saved.getRequestId());
        payload.put("createdAt", saved.getCreatedAt());
        payload.put("status", saved.getStatus());
        return ResponseEntity.ok(payload);
    }

    @Transactional
    public ResponseEntity<?> updateHrRequest(Long id, CreateHrRequestDto dto) {
        HrRequest request = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        if (!"NEW".equalsIgnoreCase(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ có thể sửa yêu cầu ở trạng thái 'NEW'"));
        }

        LocalDate minDate = LocalDate.now().plusMonths(2);
        if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
        }

        request.setRequestTitle(dto.getRequestTitle());
        request.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        request.setNote(dto.getNote());

        // xóa cũ bằng orphanRemoval
        request.getQuantityCandidates().clear();
        hrRequestRepository.flush();

        // add lại
        for (var tq : dto.getTechQuantities()) {
            Technology tech = technologyRepository.findById(tq.getTechnologyId())
                    .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: " + tq.getTechnologyId()));
            QuantityCandidate qc = new QuantityCandidate();
            qc.setHrRequest(request);
            qc.setTechnology(tech);
            qc.setSoLuong(tq.getSoLuong());
            request.getQuantityCandidates().add(qc);
        }

        HrRequest saved = hrRequestRepository.saveAndFlush(request);

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Yêu cầu nhân sự đã được cập nhật thành công!");
        payload.put("requestId", saved.getRequestId());
        payload.put("createdAt", saved.getCreatedAt());
        payload.put("status", saved.getStatus());
        return ResponseEntity.ok(payload);
    }

    // ===================== APPROVE / REJECT =====================

    @Transactional
    public HrRequestResponse approveRequest(Long id, String note) {
        HrRequest req = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        String st = req.getStatus();
        if ("APPROVED".equalsIgnoreCase(st)) {
            // idempotent
        } else if ("CANCELED".equalsIgnoreCase(st)) {
            throw new IllegalStateException("Yêu cầu đã bị từ chối, không thể phê duyệt");
        } else {
            req.setStatus("APPROVED");
            req.setNote(note);
            hrRequestRepository.save(req);
        }
        return toResponseWithTechs(req);
    }

    @Transactional
    public HrRequestResponse rejectRequest(Long id, String note) {
        HrRequest req = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã được phê duyệt, không thể từ chối");
        }
        req.setStatus("CANCELED");
        req.setNote(note);
        hrRequestRepository.save(req);

        return toResponseWithTechs(req);
    }

    // ===================== TECHNOLOGIES =====================

    @Transactional(readOnly = true)
    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }

    // ===================== PLAN DEFAULTS =====================

    @Transactional(readOnly = true)
    public PlanDefaultsDto buildPlanDefaultsFromRequest(Long requestId) {
        HrRequest req = hrRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + requestId));

        // Theo yêu cầu:
        // - tên nhu cầu hiển thị ở modal -> suggestedPlanName
        // - hạn tuyển dụng = hôm nay
        // - hạn bàn giao = expectedDeliveryDate
        var recruitmentDeadline = LocalDate.now();
        var deliveryDeadline = req.getExpectedDeliveryDate();

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

    // ===================== MAPPERS =====================

    private HrRequestResponse toResponseWithTechs(HrRequest hr) {
        List<TechQuantityDto> techs = quantityCandidateRepository.findByHrRequest_RequestId(hr.getRequestId())
                .stream()
                .map(qc -> new TechQuantityDto(qc.getTechnology().getId(), qc.getSoLuong()))
                .toList();

        return new HrRequestResponse(
                hr.getRequestId(),
                hr.getRequestTitle(),
                hr.getStatus(),
                hr.getExpectedDeliveryDate(),
                hr.getCreatedAt(),
                hr.getNote(),
                hr.getCreatedBy() != null ? hr.getCreatedBy().getFullName() : null,
                techs
        );
    }
}
