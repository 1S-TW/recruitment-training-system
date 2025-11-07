package com.example.recruitmenttrainingsystem.entity; // Đảm bảo package là '...entity'

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "recruitment_needs")
@Data
public class RecruitmentNeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "need_name", nullable = false, length = 60)
    private String needName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecruitmentNeedStatus status; // Sẽ sử dụng Enum từ '...entity'

    @Column(name = "handover_deadline")
    private LocalDate handoverDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "id")
    private User createdBy; // Đảm bảo User entity cũng nằm trong '...entity'
}