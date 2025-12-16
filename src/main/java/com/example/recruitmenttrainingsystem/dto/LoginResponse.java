package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor          // ✅ thêm constructor rỗng cho chắc
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
    private UUID id;

    // ✅ NEW: trả trạng thái hoạt động để FE biết có bị khóa hay không
    private Boolean status;
}
