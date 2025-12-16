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

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public boolean sendVerificationEmail(String to, String token, String fullName) {
        String link = frontendUrl + "/verify?token=" + token;

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
            return true;
        } catch (MessagingException e) {
            // KHÔNG throw để khỏi làm fail register trên môi trường deploy bị chặn SMTP
            System.out.println("❌ Send mail failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sendResetPasswordEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

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
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Send mail failed: " + e.getMessage());
            return false;
        }
    }
}
