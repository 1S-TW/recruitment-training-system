package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
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


}
