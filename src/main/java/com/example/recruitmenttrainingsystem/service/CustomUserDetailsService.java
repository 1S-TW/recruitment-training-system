package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("✅ User login detected: " + user.getEmail()
                + " | Role: " + user.getRole().getRoleName());

        boolean enabled = user.isStatus() && user.isEmailVerified(); // ✅ active & email verified

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),   // ✅ dùng passwordHash
                enabled,                  // ✅ isEnabled()
                true,                     // accountNonExpired
                true,                     // credentialsNonExpired
                true,                     // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()))
        );
    }
}
