package com.example.recruitmenttrainingsystem.config;
import com.example.recruitmenttrainingsystem.service.CustomUserDetailsService;
import com.example.recruitmenttrainingsystem.security.JwtAuthenticationFilter;
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
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
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
                    c.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    c.setAllowedHeaders(java.util.List.of("*"));
                    c.setAllowCredentials(true);
                    // có thể thêm expose nếu cần: c.setExposedHeaders(List.of("Authorization"));
                    return c;
                }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép toàn bộ preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ Public
                        .requestMatchers("/api/auth/**").permitAll()
                        // ✅ Business endpoints (yêu cầu role) — tách riêng từng method
                        .requestMatchers(HttpMethod.GET, "/api/hr-request/**")
                        .hasAnyRole("TRUONG_BO_PHAN","SUPER_ADMIN","HR")
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/create")
                        .hasAnyRole("TRUONG_BO_PHAN","SUPER_ADMIN","HR")
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/update/**")
                        .hasAnyRole("TRUONG_BO_PHAN","SUPER_ADMIN","HR")
                        // cái khác
                        .anyRequest().authenticated()
                );
        // ✅ Thêm JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}