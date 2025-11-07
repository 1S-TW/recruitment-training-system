package com.example.recruitmenttrainingsystem.config;

// ... các import khác ...
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- THÊM DÒNG NÀY ĐỂ KÍCH HOẠT @PreAuthorize
public class SecurityConfig {

    // ... Bean 'securityFilterChain' và các cấu hình khác của bạn ...

}