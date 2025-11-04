package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    // ✅ Lấy danh sách người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
