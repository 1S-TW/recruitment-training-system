package com.example.recruitmenttrainingsystem.repository.specification;

// VUI LÒNG KIỂM TRA CÁC IMPORT NÀY
import com.example.recruitmenttrainingsystem.entity.RecruitmentNeed;
import com.example.recruitmenttrainingsystem.entity.RecruitmentNeedStatus;
import com.example.recruitmenttrainingsystem.entity.User;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class RecruitmentNeedSpecification {

    public static Specification<RecruitmentNeed> hasNeedName(String needName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(needName)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("needName")), "%" + needName.toLowerCase() + "%");
        };
    }

    /**
     * Phương thức này giờ sẽ chấp nhận Enum từ package '...entity'
     * vì câu lệnh import ở trên.
     */
    public static Specification<RecruitmentNeed> hasStatus(RecruitmentNeedStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            // Import '...entity.RecruitmentNeedStatus' sẽ giải quyết lỗi
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<RecruitmentNeed> isCreatedBy(User user) {
        // Import '...entity.User' sẽ giải quyết mọi sự nhầm lẫn
        return (root, query, cb) -> cb.equal(root.get("createdBy"), user);
    }
}