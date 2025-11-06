package com.example.recruitmenttrainingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {

        String link = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Xác Thực Email Đăng Ký");
        msg.setText("Nhấn vào link để kích hoạt tài khoản:\n" + link);

        mailSender.send(msg);
    }
}
