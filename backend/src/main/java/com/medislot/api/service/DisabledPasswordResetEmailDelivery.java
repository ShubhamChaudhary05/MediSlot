package com.medislot.api.service;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "disabled", matchIfMissing = true)
class DisabledPasswordResetEmailDelivery implements PasswordResetEmailDelivery {
    @Override public boolean isAvailable() { return false; }
    @Override public void send(String recipientEmail, URI resetLink) { /* Deliberately do nothing. */ }
}
