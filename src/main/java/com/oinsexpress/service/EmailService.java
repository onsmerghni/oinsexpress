package com.oinsexpress.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${oinsexpress.mail.from}")
    private String fromEmail;

    @Async
    public void sendVerificationCode(String to, String firstName, String code) {
        String subject = "OINSExpress — Code de vérification";
        String body = String.format("""
            Bonjour %s,
            
            Bienvenue sur OINSExpress !
            
            Votre code de vérification est : %s
            
            Ce code est valable pendant 15 minutes.
            
            Si vous n'avez pas créé de compte, ignorez ce message.
            
            ---
            L'équipe OINSExpress
            PFA 2026 — ISTIC
            """, firstName, code);

        sendEmail(to, subject, body);
    }

    @Async
    public void sendResetPasswordCode(String to, String firstName, String code) {
        String subject = "OINSExpress — Réinitialisation du mot de passe";
        String body = String.format("""
            Bonjour %s,
            
            Vous avez demandé une réinitialisation de mot de passe.
            
            Votre code est : %s
            
            Ce code est valable pendant 15 minutes.
            
            Si vous n'avez pas fait cette demande, ignorez ce message.
            
            ---
            L'équipe OINSExpress
            """, firstName, code);

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email à {} : {}", to, e.getMessage());
            // En dev, on log le code pour que le développeur puisse tester sans SMTP
            log.warn("CODE EMAIL (DEV) : {}", body);
        }
    }
}
