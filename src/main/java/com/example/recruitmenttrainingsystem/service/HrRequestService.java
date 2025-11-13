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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HrRequestService {

    private final HrRequestRepository hrRequestRepository;
    private final TechnologyRepository technologyRepository;
    private final QuantityCandidateRepository quantityCandidateRepository;
    private final UserRepository userRepository;

    // ---------------- basic list / get ----------------
    @Transactional(readOnly = true)
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HrRequestResponse getById(Long id) {
        // dùng query fetch để tránh N+1 khi cần quantityCandidates + technology
        HrRequest hr = hrRequestRepository.findByIdWithTechs(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + id));
        return mapEntityToResponse(hr);
    }

    // ---------------- create ----------------
    @Transactional
    public ResponseEntity<?> createHrRequest(CreateHrRequestDto dto) {
        // 1️⃣ Kiểm tra null
        if (dto.getCreatedBy() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "createdBy không được null"));
        }

        // 2️⃣ Kiểm tra User tồn tại
        Optional<User> userOpt = userRepository.findById(dto.getCreatedBy());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found với UUID: " + dto.getCreatedBy()));
        }
        User user = userOpt.get();

        // 3️⃣ Kiểm tra thời hạn
        LocalDate minDate = LocalDate.now().plusMonths(2);
        if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
        }

        // 4️⃣ Tạo HrRequest
        HrRequest req = new HrRequest();
        req.setRequestTitle(dto.getRequestTitle());
        req.setStatus("NEW");
        req.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        req.setNote(dto.getNote());
        req.setCreatedBy(user);

        HrRequest saved = hrRequestRepository.save(req);

        // 5️⃣ Lưu quantity candidates
        if (dto.getTechQuantities() != null) {
            for (var tq : dto.getTechQuantities()) {
                Technology tech = technologyRepository.findById(tq.getTechnologyId())
                        .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: " + tq.getTechnologyId()));
                QuantityCandidate qc = new QuantityCandidate();
                qc.setHrRequest(saved);
                qc.setTechnology(tech);
                qc.setSoLuong(tq.getSoLuong());
                quantityCandidateRepository.save(qc);
            }
        }

        // 6️⃣ Trả payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Yêu cầu nhân sự đã được tạo thành công!");
        payload.put("requestId", saved.getRequestId());
        payload.put("createdAt", saved.getCreatedAt());
        payload.put("status", saved.getStatus());
        return ResponseEntity.ok(payload);
    }

    // ---------------- update ----------------
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

        // Xoá danh sách cũ
        // Nếu HrRequest.quantityCandidates có Cascade + orphanRemoval thì clear() + save sẽ xóa bản ghi cũ
        request.getQuantityCandidates().clear();

        // Force flush để đồng bộ nếu cần (thận trọng: chỉ dùng nếu bạn hiểu cascade/orphan)
        hrRequestRepository.flush();

        // Thêm lại
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

    // ---------------- approve / reject ----------------
    @Transactional
    public HrRequestResponse approveRequest(Long id, String note) {
        HrRequest req = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            // idempotent: không làm gì
        } else if ("CANCELED".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã bị từ chối, không thể phê duyệt");
        } else {
            req.setStatus("APPROVED");
            req.setNote(note);
            hrRequestRepository.save(req);
        }
        return mapEntityToResponse(req);
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

        return mapEntityToResponse(req);
    }

    // ---------------- utilities ----------------
    @Transactional(readOnly = true)
    public List<Technology> getTechnologies() {
        return technologyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PlanDefaultsDto buildPlanDefaultsFromRequest(Long requestId) {
        HrRequest req = hrRequestRepository.findByIdWithTechs(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + requestId));

        LocalDate recruitmentEnd = LocalDate.now().plusDays(14);
        LocalDate deliveryDeadline = req.getExpectedDeliveryDate();

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
        dto.setRecruitmentDeadline(recruitmentEnd);
        dto.setDeliveryDeadline(deliveryDeadline);
        dto.setNote(req.getNote());
        dto.setTotalCandidates(total);
        dto.setTechQuantities(techDetails);
        return dto;
    }

    // ---------------- search (Specification) ----------------
    @Transactional(readOnly = true)
    public List<HrRequestResponse> searchHrRequests(String title, String status) {
        Specification<HrRequest> spec = null;

        if (title != null && !title.isBlank()) {
            spec = (spec == null) ? hasTitleLike(title) : spec.and(hasTitleLike(title));
        }

        if (status != null && !status.isBlank()) {
            spec = (spec == null) ? hasStatus(status) : spec.and(hasStatus(status));
        }

        // nếu không có điều kiện trả về toàn bộ (có sort)
        if (spec == null) {
            return hrRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream()
                    .map(this::mapEntityToResponse)
                    .toList();
        }

        // findAll(spec, sort) có sẵn khi repo extends JpaSpecificationExecutor
        return hrRequestRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    private Specification<HrRequest> hasTitleLike(String title) {
        String pattern = "%" + title.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("requestTitle")), pattern);
    }

    private Specification<HrRequest> hasStatus(String status) {
        String s = status.toLowerCase();
        return (root, query, cb) -> cb.equal(cb.lower(root.get("status")), s);
    }

    // ---------------- mapper (safe) ----------------
    private HrRequestResponse mapEntityToResponse(HrRequest hr) {
        // an toàn: nếu hr null hoặc createdBy null thì tránh NPE
        List<TechQuantityDto> techs = quantityCandidateRepository.findByHrRequest_RequestId(hr.getRequestId())
                .stream()
                .map(qc -> new TechQuantityDto(qc.getTechnology().getId(), qc.getSoLuong()))
                .toList();

        String creatorName = null;
        if (hr.getCreatedBy() != null) {
            creatorName = hr.getCreatedBy().getFullName();
        }

        return new HrRequestResponse(
                hr.getRequestId(),
                hr.getRequestTitle(),
                hr.getStatus(),
                hr.getExpectedDeliveryDate(),
                hr.getCreatedAt(),
                hr.getNote(),
                creatorName,
                techs
        );
    }
}
