package com.example.recruitmenttrainingsystem.config;

import com.example.recruitmenttrainingsystem.entity.Role;
import com.example.recruitmenttrainingsystem.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRoles(RoleRepository repo) {
        return args -> {
            String[][] roles = {
                    {"SUPER_ADMIN", "Toàn quyền hệ thống"},
                    {"LEAD", "Trưởng nhóm / bộ phận"},
                    {"QLDT", "Quản lý đào tạo"},
                    {"HR", "Nhân sự (mặc định)"}
            };

            for (String[] r : roles) {
                repo.findByRoleName(r[0])
                        .orElseGet(() -> repo.save(
                                Role.builder().roleName(r[0]).description(r[1]).build()
                        ));
            }
        };
    }
}