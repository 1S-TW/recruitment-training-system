package com.example.recruitmenttrainingsystem.config;

import com.example.recruitmenttrainingsystem.security.JwtFilter;
import com.example.recruitmenttrainingsystem.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var c = new org.springframework.web.cors.CorsConfiguration();
                    c.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
                    c.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    c.setAllowedHeaders(java.util.List.of("*"));
                    c.setAllowCredentials(true);
                    return c;
                }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // --- 1. NHU CẦU NHÂN SỰ (HrRequest) ---
                        // Xem: Admin, LEAD, QLDT (HR bị cấm)
                        .requestMatchers(HttpMethod.GET, "/api/hr-request/**").hasAnyRole("SUPER_ADMIN", "LEAD", "QLDT")
                        // Tạo/Sửa: Admin, LEAD
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/create").hasAnyRole("SUPER_ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/update/**").hasAnyRole("SUPER_ADMIN", "LEAD")
                        // Duyệt/Từ chối: Admin, QLDT
                        .requestMatchers(HttpMethod.PUT, "/api/hr-request/*/approve").hasAnyRole("SUPER_ADMIN", "QLDT")
                        .requestMatchers(HttpMethod.PUT, "/api/hr-request/*/reject").hasAnyRole("SUPER_ADMIN", "QLDT")

                        // --- 2. KẾ HOẠCH TUYỂN DỤNG (RecruitmentPlan) ---
                        // Xem: Tất cả
                        .requestMatchers(HttpMethod.GET, "/api/recruitment-plans/**").hasAnyRole("SUPER_ADMIN", "LEAD", "QLDT", "HR")
                        // Tạo: Admin, QLDT
                        .requestMatchers(HttpMethod.POST, "/api/recruitment-plans").hasAnyRole("SUPER_ADMIN", "QLDT")
                        // Phê duyệt/Từ chối: Admin, HR
                        .requestMatchers(HttpMethod.PUT, "/api/recruitment-plans/*/confirm").hasAnyRole("SUPER_ADMIN", "HR")
                        .requestMatchers(HttpMethod.POST, "/api/recruitment-plans/*/reject").hasAnyRole("SUPER_ADMIN", "HR")

                        // --- 3. ỨNG VIÊN (Candidate) ---
                        // ✅ [FIX] Cho phép LEAD xem (GET) để timeline hiển thị đúng số lượng ứng viên
                        .requestMatchers(HttpMethod.GET, "/api/candidates/**").hasAnyRole("SUPER_ADMIN", "QLDT", "HR", "LEAD")
                        // Tạo: Admin, HR
                        .requestMatchers(HttpMethod.POST, "/api/candidates/create").hasAnyRole("SUPER_ADMIN", "HR")
                        // Sửa/Chấm điểm: Admin, QLDT, HR
                        .requestMatchers(HttpMethod.PUT, "/api/candidates/*/save-result").hasAnyRole("SUPER_ADMIN", "QLDT", "HR")

                        // --- 4. ĐÀO TẠO (Training) ---
                        // ✅ [FIX] Cho phép LEAD và HR xem (GET) để timeline hiển thị đúng tiến độ đào tạo/bàn giao
                        .requestMatchers(HttpMethod.GET, "/api/trainings/**").hasAnyRole("SUPER_ADMIN", "QLDT", "HR", "LEAD")
                        // Các thao tác sửa đổi (PUT/POST/DELETE) vẫn chỉ dành cho Admin và QLDT
                        .requestMatchers("/api/trainings/**").hasAnyRole("SUPER_ADMIN", "QLDT")

                        // Admin Only endpoints
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}