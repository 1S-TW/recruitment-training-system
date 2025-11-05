package com.example.recruitmenttrainingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom("no-reply@aristole.com");
            helper.setSubject("Aristole - Xác thực email đăng ký");
            String verificationUrl = "http://localhost:3000/verify?token=" + token;
            helper.setText("""
                <html>
                <body>
                    <h2>Xin chào từ Aristole!</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại Aristole Recruitment & Training System.</p>
                    <p>Vui lòng <a href="%s">nhấn vào đây</a> để xác thực email của bạn.</p>
                    <p>Liên kết sẽ hết hạn sau 24 giờ.</p>
                    <hr>
                    <p>Trân trọng,<br>Team Aristole</p>
                </body>
                </html>
                """.formatted(verificationUrl), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác thực", e);
        }
    }

    public void sendResetPasswordEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom("no-reply@aristole.com");
            helper.setSubject("Aristole - Đặt lại mật khẩu");
            String resetUrl = "http://localhost:3000/reset-password?token=" + token;
            helper.setText("""
                <html>
                <body>
                    <h2>Aristole - Đặt lại mật khẩu</h2>
                    <p>Bạn đã yêu cầu đặt lại mật khẩu.</p>
                    <p>Vui lòng <a href="%s">nhấn vào đây</a> để đặt mật khẩu mới.</p>
                    <p>Liên kết sẽ hết hạn sau 1 giờ.</p>
                    <hr>
                    <p>Trân trọng,<br>Team Aristole</p>
                </body>
                </html>
                """.formatted(resetUrl), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        }
    }
}