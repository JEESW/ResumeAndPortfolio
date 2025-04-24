package com.example.resumeandportfolio.util.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Mail Utility
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Component
public class MailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${application.server.url}")
    private String sendingUrl;

    public MailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 회원 가입 시 인증 메일 보내는 메서드
    public void sendVerificationMail(String toEmail, String token) {
        String subject = "[ResumeAndPortfolio] 이메일 인증";
        String verificationLink = sendingUrl + "/signup?token=" + token;
        String text =
            "저희 서비스에 회원가입 해주셔서 감사합니다! 다음 이메일 인증 링크를 눌러주세요!:\n"
                + verificationLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    // 비밀번호 재설정 시 인증 메일 보내는 메서드
    public void sendPasswordResetMail(String toEmail, String token) {
        String subject = "[ResumeAndPortfolio] 비밀번호 재설정";
        String resetLink = sendingUrl + "/reset-password?token=" + token;
        String text = "비밀번호 재설정을 요청하셨습니다. 아래 링크를 클릭하여 새로운 비밀번호를 설정하세요:\n" + resetLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}