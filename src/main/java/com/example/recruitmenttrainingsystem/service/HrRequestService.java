    // src/main/java/com/example/recruitmenttrainingsystem/service/HrRequestService.java
    package com.example.recruitmenttrainingsystem.service;

    import com.example.recruitmenttrainingsystem.dto.CreateHrRequestDto;
    import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
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
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
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

        // LẤY DANH SÁCH YÊU CẦU — sort theo createdAt DESC để item mới ở đầu
        @Transactional(readOnly = true)
        public List<HrRequestResponse> getAllHrRequests() {
            return hrRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream()
                    .map(hr -> new HrRequestResponse(
                            hr.getRequestId(),
                            hr.getRequestTitle(),
                            hr.getStatus(),
                            hr.getExpectedDeliveryDate(),
                            hr.getCreatedAt(),
                            hr.getNote(),
                            hr.getCreatedBy().getFullName(),
                            hr.getQuantityCandidates().stream()
                                    .map(qc -> new TechQuantityDto(qc.getTechnology().getId(), qc.getSoLuong()))
                                    .toList()
                    ))
                    .toList();
        }

        // TẠO YÊU CẦU (mặc định NEW)
        @Transactional
        public ResponseEntity<?> createHrRequest(CreateHrRequestDto dto) {
            // ✅ lấy user hiện tại từ SecurityContext thay vì hard-code 1L
            User user = getCurrentUserOrThrow();

            LocalDate minDate = LocalDate.now().plusMonths(2);
            if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
            }

            HrRequest request = new HrRequest();
            request.setRequestTitle(dto.getRequestTitle());
            request.setStatus("NEW"); // mặc định NEW
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

            // ✅ trả thêm dữ liệu FE có thể dùng để chèn ngay
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", "Yêu cầu nhân sự đã được tạo thành công!");
            payload.put("requestId", savedRequest.getRequestId());
            payload.put("createdAt", savedRequest.getCreatedAt());
            payload.put("status", savedRequest.getStatus());
            return ResponseEntity.ok(payload);
        }

        // LẤY DANH SÁCH CÔNG NGHỆ
        @Transactional(readOnly = true)
        public List<Technology> getTechnologies() {
            return technologyRepository.findAll();
        }

        // CẬP NHẬT YÊU CẦU (chỉ khi NEW)
        @Transactional
        public ResponseEntity<?> updateHrRequest(Long id, CreateHrRequestDto dto) {
            HrRequest request = hrRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

            // Chỉ cho sửa khi NEW
            if (!"NEW".equals(request.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ có thể sửa yêu cầu ở trạng thái 'NEW'"));
            }

            LocalDate minDate = LocalDate.now().plusMonths(2);
            if (dto.getExpectedDeliveryDate().isBefore(minDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Thời hạn bàn giao phải cách ít nhất 2 tháng từ hôm nay"));
            }

            // cập nhật các field đơn
            request.setRequestTitle(dto.getRequestTitle());
            request.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
            request.setNote(dto.getNote());

            // ❗️XÓA CŨ BẰNG orphanRemoval (không gọi repository.deleteAll)
            request.getQuantityCandidates().clear();
            hrRequestRepository.flush(); // đảm bảo DELETE chạy ngay

            // ❗️TẠO MỚI: add vào collection + set quan hệ (cascade ALL sẽ tự persist)
            for (var tq : dto.getTechQuantities()) {
                Technology tech = technologyRepository.findById(tq.getTechnologyId())
                        .orElseThrow(() -> new RuntimeException("Công nghệ không tồn tại: ID = " + tq.getTechnologyId()));
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

        // ===== Helper =====
        private User getCurrentUserOrThrow() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : null;
            if (email == null) throw new RuntimeException("Không xác định được người dùng hiện tại");

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
        }
    }
