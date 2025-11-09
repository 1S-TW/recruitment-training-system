// src/main/java/com/example/recruitmenttrainingsystem/entity/RequestStatus.java
package com.example.recruitmenttrainingsystem.entity;

public enum RequestStatus {
    DANG_CHO("Đang chờ"),
    DA_HUY("Đã hủy"),
    DA_XAC_NHAN("Đã xác nhận"),
    BI_TU_CHOI("Bị từ chối"),
    BI_TU_CHOI_DCAN("Bị từ chối (HR)"),
    DANG_TUYEN_DUNG("Đang tuyển dụng"),
    DA_BAN_GIAO("Đã bàn giao");

    private final String label;
    RequestStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}