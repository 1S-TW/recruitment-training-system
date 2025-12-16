// src/main/java/com/example/recruitmenttrainingsystem/service/NotificationService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.NotificationRepository;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ===== Helper chung: gửi cho tất cả user có role =====
    private void notifyRole(
            String roleName,
            User actor,
            String eventType,
            String title,
            String content,
            String refType,
            Long refId
    ) {
        List<User> receivers = userRepository.findByRole_RoleNameAndStatusTrue(roleName);

        for (User u : receivers) {
            Notification n = Notification.builder()
                    .sender(actor)
                    .receiver(u)
                    .eventType(eventType)
                    .title(title)
                    .content(content)
                    .referenceType(refType)
                    .referenceId(refId)
                    .createdAt(Instant.now())
                    .read(false)
                    .build();
            notificationRepository.save(n);
        }
    }

    // ====== 1. Tạo nhu cầu nhân sự (LEAD -> HR) ======
    public void notify_HrRequestCreated(HrRequest req, User lead) {
        String title = "Nhu cầu nhân sự mới";
        String content = "LEAD " + lead.getFullName()
                + " vừa tạo nhu cầu: \"" + req.getRequestTitle() + "\".";
        notifyRole("HR", lead,
                "HR_REQUEST_CREATED",
                title, content,
                "HR_REQUEST", req.getRequestId());
    }

    // ====== 2. HR duyệt nhu cầu (HR -> LEAD) ======
    public void notify_HrRequestApproved(HrRequest req, User hr) {
        String title = "Nhu cầu nhân sự đã được duyệt";
        String content = "HR " + hr.getFullName()
                + " đã duyệt nhu cầu: \"" + req.getRequestTitle() + "\".";
        notifyRole("LEAD", hr,
                "HR_REQUEST_APPROVED",
                title, content,
                "HR_REQUEST", req.getRequestId());
    }

    // ====== 3. HR từ chối nhu cầu (HR -> LEAD) ======
    public void notify_HrRequestRejected(HrRequest req, User hr) {
        String title = "Nhu cầu nhân sự bị từ chối";
        String content = "HR " + hr.getFullName()
                + " đã từ chối nhu cầu: \"" + req.getRequestTitle() + "\".";
        notifyRole("LEAD", hr,
                "HR_REQUEST_REJECTED",
                title, content,
                "HR_REQUEST", req.getRequestId());
    }

    // ====== 4. HR tạo kế hoạch (HR -> QLDT) ======
    public void notify_PlanCreated(RecruitmentPlan plan, User hr) {
        String title = "Kế hoạch tuyển dụng mới";
        String content = "HR " + hr.getFullName()
                + " vừa tạo kế hoạch: \"" + plan.getPlanName() + "\".";
        notifyRole("QLDT", hr,
                "PLAN_CREATED",
                title, content,
                "RECRUITMENT_PLAN", plan.getRecruitmentPlanId());
    }

    // ====== 5. QLDT duyệt kế hoạch (QLDT -> HR + LEAD) ======
    public void notify_PlanConfirmed(RecruitmentPlan plan, User qldt) {
        Long planId = plan.getRecruitmentPlanId();

        // 🔹 Thông báo cho HR – nhấn mạnh "kế hoạch bạn phụ trách"
        String titleHr = "Kế hoạch tuyển dụng bạn phụ trách đã được duyệt";
        String contentHr = "QLĐT " + qldt.getFullName()
                + " đã duyệt kế hoạch bạn phụ trách: \"" + plan.getPlanName() + "\".";

        notifyRole("HR", qldt,
                "PLAN_CONFIRMED",
                titleHr, contentHr,
                "RECRUITMENT_PLAN", planId);

        // 🔹 Thông báo cho LEAD – giữ nguyên format bạn đã dùng
        String titleLead = "Kế hoạch tuyển dụng đã được duyệt";
        String contentLead = "QLĐT " + qldt.getFullName()
                + " đã duyệt kế hoạch: \"" + plan.getPlanName() + "\".";

        notifyRole("LEAD", qldt,
                "PLAN_CONFIRMED",
                titleLead, contentLead,
                "RECRUITMENT_PLAN", planId);
    }

    // ====== 6. QLDT từ chối kế hoạch (QLDT -> HR + LEAD) ======
    public void notify_PlanRejected(RecruitmentPlan plan, User qldt) {
        Long planId = plan.getRecruitmentPlanId();

        // 🔹 Thông báo cho HR
        String titleHr = "Kế hoạch tuyển dụng bạn phụ trách bị từ chối";
        String contentHr = "QLĐT " + qldt.getFullName()
                + " đã từ chối kế hoạch bạn phụ trách: \"" + plan.getPlanName() + "\".";

        notifyRole("HR", qldt,
                "PLAN_REJECTED",
                titleHr, contentHr,
                "RECRUITMENT_PLAN", planId);

        // 🔹 Thông báo cho LEAD – giữ nguyên format
        String titleLead = "Kế hoạch tuyển dụng bị từ chối";
        String contentLead = "QLĐT " + qldt.getFullName()
                + " đã từ chối kế hoạch: \"" + plan.getPlanName() + "\".";

        notifyRole("LEAD", qldt,
                "PLAN_REJECTED",
                titleLead, contentLead,
                "RECRUITMENT_PLAN", planId);
    }
}
