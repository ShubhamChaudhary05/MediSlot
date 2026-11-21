package com.medislot.api.service;

import com.medislot.api.config.AppProperties;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Local-only reset-link sink. It is ignored by Git and must never be used in production. */
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "development-file")
class DevelopmentFilePasswordResetEmailDelivery implements PasswordResetEmailDelivery {
    private final AppProperties properties;
    DevelopmentFilePasswordResetEmailDelivery(AppProperties properties) { this.properties = properties; }
    @Override public boolean isAvailable() { return true; }
    @Override public void send(String recipientEmail, URI resetLink) {
        try {
            Path outbox = Path.of(properties.getMail().getDevelopmentOutbox()).normalize();
            Files.createDirectories(outbox);
            Path message = outbox.resolve("reset-" + UUID.randomUUID() + ".txt");
            Files.writeString(message, "To: " + recipientEmail + System.lineSeparator() + "Reset link: " + resetLink + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new IllegalStateException("Development reset outbox is unavailable.", exception);
        }
    }
}
