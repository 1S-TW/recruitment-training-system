// src/main/java/com/example/recruitmenttrainingsystem/config/SecurityConfig.java
package com.example.recruitmenttrainingsystem.config;

import com.example.recruitmenttrainingsystem.security.JwtFilter;
import com.example.recruitmenttrainingsystem.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

    // ✅ CORS chuẩn cho Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://recruitment-training-system-fe.onrender.com",
                "https://*.onrender.com"
        ));

        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        // nếu FE cần đọc Authorization header từ response
        c.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ===== PUBLIC ENDPOINTS =====
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // --- 1. HR REQUEST ---
                        .requestMatchers(HttpMethod.GET, "/api/hr-request/**")
                        .hasAnyRole("SUPER_ADMIN", "LEAD", "QLDT", "HR")
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/create")
                        .hasAnyRole("SUPER_ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.POST, "/api/hr-request/update/**")
                        .hasAnyRole("SUPER_ADMIN", "LEAD")
                        .requestMatchers(HttpMethod.PUT, "/api/hr-request/*/approve")
                        .hasAnyRole("SUPER_ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/hr-request/*/reject")
                        .hasAnyRole("SUPER_ADMIN", "HR")

                        // --- 2. RECRUITMENT PLAN ---
                        .requestMatchers(HttpMethod.GET, "/api/recruitment-plans/**")
                        .hasAnyRole("SUPER_ADMIN", "LEAD", "QLDT", "HR")
                        .requestMatchers(HttpMethod.POST, "/api/recruitment-plans")
                        .hasAnyRole("SUPER_ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/recruitment-plans/*/confirm")
                        .hasAnyRole("SUPER_ADMIN", "QLDT")
                        .requestMatchers(HttpMethod.POST, "/api/recruitment-plans/*/reject")
                        .hasAnyRole("SUPER_ADMIN", "QLDT")

                        // --- 3. CANDIDATE ---
                        .requestMatchers(HttpMethod.GET, "/api/candidates/**")
                        .hasAnyRole("SUPER_ADMIN", "QLDT", "HR", "LEAD")
                        .requestMatchers(HttpMethod.POST, "/api/candidates/create")
                        .hasAnyRole("SUPER_ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/candidates/*/save-result")
                        .hasAnyRole("SUPER_ADMIN", "QLDT", "HR")

                        // --- 4. TRAINING ---
                        .requestMatchers(HttpMethod.GET, "/api/trainings/**")
                        .hasAnyRole("SUPER_ADMIN", "QLDT", "LEAD", "HR")
                        .requestMatchers("/api/trainings/**")
                        .hasAnyRole("SUPER_ADMIN", "QLDT")

                        // --- 5. NOTIFICATION ---
                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("SUPER_ADMIN", "LEAD", "QLDT", "HR")

                        // --- 6. ADMIN ONLY ---
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
