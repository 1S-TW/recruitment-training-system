package com.example.recruitmenttrainingsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // ✅ Link trỏ về FE (deploy) — default vẫn là localhost để chạy local
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private String normalizeBaseUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    // ✅ Async để không block request /register
    @Async
    public void sendVerificationEmail(String to, String token, String fullName) {
        String base = normalizeBaseUrl(frontendUrl);
        String link = base + "/verify?token=" + token;

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
            log.info("✅ Sent verification email to {}", to);
        } catch (MessagingException e) {
            // ❗Không throw RuntimeException nữa để tránh ảnh hưởng flow đăng ký
            log.error("❌ Cannot send verification email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error when sending verification email to {}: {}", to, e.getMessage(), e);
        }
    }

    // ✅ Async để không block request /forgot-password
    @Async
    public void sendResetPasswordEmail(String to, String token) {
        String base = normalizeBaseUrl(frontendUrl);
        String link = base + "/reset-password?token=" + token;

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
            log.info("✅ Sent reset-password email to {}", to);
        } catch (MessagingException e) {
            log.error("❌ Cannot send reset-password email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error when sending reset-password email to {}: {}", to, e.getMessage(), e);
        }
    }
}
