package com.example.recruitmenttrainingsystem.config;

import com.example.recruitmenttrainingsystem.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // PHÂN QUYỀN TẠI ĐÂY
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify",
                                "/error",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll() // 1. Các endpoint public

                        // 2. Các endpoint cho ADMIN
                        .requestMatchers("/api/admin/**")
                        .hasRole("SUPER_ADMIN") // Chỉ SUPER_ADMIN

                        .requestMatchers("/api/hr-request/**").hasAnyRole("LEAD","SUPER_ADMIN","HR")

                        .requestMatchers("/api/recruitment-plans/**").hasAnyRole("QLDT","SUPER_ADMIN","HR")

                        // 3. Các endpoint cho user đã đăng nhập (ví dụ: đổi mật khẩu)
                        .requestMatchers("/api/user/**")
                        .authenticated() // Bất kỳ ai đã đăng nhập

                        // 4. Tất cả các request khác (nếu có)
                        .anyRequest().authenticated() // Cần đăng nhập
        );
        // --- KẾT THÚC CẬP NHẬT ---

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
