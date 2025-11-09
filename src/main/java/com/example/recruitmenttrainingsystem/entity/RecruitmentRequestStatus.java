package com.example.recruitmenttrainingsystem.entity;

public enum RecruitmentRequestStatus {
    PENDING,          // Vừa được PM khởi tạo
    APPROVED_BY_DET,  // QLDT phê duyệt
    REJECTED_BY_DET,  // QLDT từ chối
    REJECTED_BY_DCAN, // DCAN từ chối (sau khi lên kế hoạch)
    RECRUITING,       // DCAN phê duyệt kế hoạch
    COMPLETED,        // Bàn giao đủ người
    CANCELLED         // PM hủy
}