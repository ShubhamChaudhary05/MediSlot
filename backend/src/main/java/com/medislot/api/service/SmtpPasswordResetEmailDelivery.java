package com.medislot.api.service;

import com.medislot.api.config.AppProperties;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "smtp")
class SmtpPasswordResetEmailDelivery implements PasswordResetEmailDelivery {
    private final JavaMailSender mailSender;
    private final AppProperties properties;
    SmtpPasswordResetEmailDelivery(JavaMailSender mailSender, AppProperties properties) { this.mailSender = mailSender; this.properties = properties; }
    @Override public boolean isAvailable() { return properties.getMail().getFrom() != null && !properties.getMail().getFrom().isBlank(); }
    @Override public void send(String recipientEmail, URI resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Reset your MediSlot password");
        message.setText("A password reset was requested for your MediSlot account. This link expires soon and can be used once:\n\n" + resetLink + "\n\nIf you did not request this, you can ignore this email.");
        mailSender.send(message);
    }
}
