package com.farmatodo.challenge.infrastructure.adapter.notification;

import com.farmatodo.challenge.domain.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;

    // Hacemos el envío ASÍNCRONO para no bloquear el hilo de la compra.

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Iniciando envío de correo a: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@farmatodo.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("✅ Correo enviado exitosamente a {}", to);
        } catch (Exception e) {
            log.error("❌ Error enviando correo a {}: {}", to, e.getMessage());

        }
    }
}