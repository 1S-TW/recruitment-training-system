// Tên file: service/EmailService.java
package com.example.recruitmenttrainingsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thêm TemplateEngine

    // --- SỬA HÀM NÀY ---
    public void sendVerificationEmail(String to, String token, String fullName) {
        // Đây là URL trỏ về trang React (Frontend) của bạn
        // Nó sẽ được xử lý bởi React Router
        String link = "http://localhost:5173/verify?token=" + token;

        // Chuẩn bị các biến để truyền vào template
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationLink", link);

        // Render template HTML
        String htmlContent = templateEngine.process("verification-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Xác Thực Email Đăng Ký - Recruitment Training System");
            helper.setText(htmlContent, true); // true = Bật chế độ HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Nên log lỗi này ra
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    // --- SỬA HÀM NÀY ---
    public void sendResetPasswordEmail(String to, String token) {
        // Tương tự, đây là URL trỏ về trang React
        String link = "http://localhost:5173/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("resetLink", link);
        // Bạn cũng nên truyền "fullName" vào đây nếu muốn
        // context.setVariable("fullName", "Tên User"); 

        // Giả sử bạn có template tên là 'reset-password-email.html'
        String htmlContent = templateEngine.process("reset-password-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Khôi phục mật khẩu - Recruitment Training System");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }
}