package com.medislot.api.service;

import java.net.URI;

/** Delivers a reset link without ever persisting or logging the raw token. */
public interface PasswordResetEmailDelivery {
    boolean isAvailable();
    void send(String recipientEmail, URI resetLink);
}
