package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.RecruitmentNeedDto;
// Đảm bảo các import này là chính xác
import com.example.recruitmenttrainingsystem.entity.RecruitmentNeed;
import com.example.recruitmenttrainingsystem.entity.RecruitmentNeedStatus;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.RecruitmentNeedRepository;
import com.example.recruitmenttrainingsystem.repository.specification.RecruitmentNeedSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruitmentNeedService {

    @Autowired
    private RecruitmentNeedRepository recruitmentNeedRepository;

    @Transactional(readOnly = true)
    public Page<RecruitmentNeedDto> searchNeeds(String needName, RecruitmentNeedStatus status, User pmUser, Pageable pageable) {

        Specification<RecruitmentNeed> spec = Specification
                .where(RecruitmentNeedSpecification.isCreatedBy(pmUser))
                .and(RecruitmentNeedSpecification.hasNeedName(needName))
                .and(RecruitmentNeedSpecification.hasStatus(status)); // Điều này sẽ hoạt động

        // Dòng này bây giờ sẽ được giải quyết vì Repository đã được sửa
        Page<RecruitmentNeed> entityPage = recruitmentNeedRepository.findAll(spec, pageable);

        return entityPage.map(this::convertToDto);
    }

    private RecruitmentNeedDto convertToDto(RecruitmentNeed entity) {
        RecruitmentNeedDto dto = new RecruitmentNeedDto();
        dto.setId(entity.getId());
        dto.setNeedName(entity.getNeedName());
        dto.setStatus(entity.getStatus());
        dto.setHandoverDeadline(entity.getHandoverDeadline());
        if (entity.getCreatedBy() != null) {
            dto.setCreatedByUsername(entity.getCreatedBy().getUsername());
        }
        return dto;
    }
}