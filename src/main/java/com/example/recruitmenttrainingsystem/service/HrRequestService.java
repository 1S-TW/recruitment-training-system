package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import com.example.recruitmenttrainingsystem.repository.HrRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrRequestService {

    private final HrRequestRepository hrRequestRepository;

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

    // ✅ Phê duyệt yêu cầu
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

    // ❌ Từ chối yêu cầu
    public HrRequestResponse rejectRequest(Long id, String note) {
        HrRequest hrRequest = hrRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + id));

        hrRequest.setStatus("REJECTED");
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
