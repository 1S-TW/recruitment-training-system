// src/main/java/com/example/recruitmenttrainingsystem/service/EmailService.java
package com.example.recruitmenttrainingsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Lấy từ application.properties -> app.frontend.url -> FRONTEND_URL
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // Cho phép tắt mail khi deploy (tránh timeout SMTP trên Render)
    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public void sendVerificationEmail(String to, String token, String fullName) {
        String link = frontendUrl + "/verify?token=" + token;

        // Nếu tắt mail thì không gửi, tránh request bị treo
        if (!mailEnabled) {
            System.out.println("[MAIL DISABLED] Verification link for " + to + ": " + link);
            return;
        }

        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationLink", link);

        String htmlContent = templateEngine.process("verification-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Xác Thực Email Đăng Ký - Recruitment Training System");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    public void sendResetPasswordEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        if (!mailEnabled) {
            System.out.println("[MAIL DISABLED] Reset link for " + to + ": " + link);
            return;
        }

        Context context = new Context();
        context.setVariable("resetLink", link);

        String htmlContent = templateEngine.process("reset-password-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Khôi phục mật khẩu - Recruitment Training System");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }
}
