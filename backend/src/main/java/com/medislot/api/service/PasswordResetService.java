package com.medislot.api.service;

import com.medislot.api.config.AppProperties;
import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.PasswordResetToken;
import com.medislot.api.dto.AuthDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.PasswordResetTokenRepository;
import com.medislot.api.repository.UserRepository;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordResetEmailDelivery delivery;
    private final AppProperties properties;
    private final org.springframework.security.crypto.password.PasswordEncoder passwords;
    private final AuditService audit;
    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, PasswordResetEmailDelivery delivery, AppProperties properties, org.springframework.security.crypto.password.PasswordEncoder passwords, AuditService audit) {
        this.users = users; this.tokens = tokens; this.delivery = delivery; this.properties = properties; this.passwords = passwords; this.audit = audit;
    }
    @Transactional
    public void request(AuthDtos.ForgotPasswordRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        if (user == null || !delivery.isAvailable()) return;
        Instant now = Instant.now();
        if (tokens.countByUserIdAndCreatedAtAfter(user.getId(), now.minus(Duration.ofMinutes(properties.getSecurity().getPasswordResetMinutes()))) >= properties.getSecurity().getPasswordResetMaxRequests()) return;
        tokens.invalidateActiveForUser(user.getId(), now);
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        tokens.save(new PasswordResetToken(user, sha256(rawToken), now.plus(Duration.ofMinutes(properties.getSecurity().getPasswordResetMinutes()))));
        URI link = UriComponentsBuilder.fromUriString(properties.getMail().getResetUrl()).queryParam("token", rawToken).build().encode().toUri();
        try {
            delivery.send(user.getEmail(), link);
            audit.record(user, "PASSWORD_RESET_REQUESTED", "USER", user.getId(), "Reset link delivery requested");
        } catch (RuntimeException ignored) {
            // Preserve the generic response and never log reset tokens or recipient identifiers.
            // A link that was not delivered must not remain usable.
            tokens.invalidateActiveForUser(user.getId(), Instant.now());
        }
    }
    @Transactional
    public void reset(AuthDtos.ResetPasswordRequest request) {
        PasswordResetToken token = tokens.findByTokenHash(sha256(request.token())).orElseThrow(this::invalidToken);
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) throw invalidToken();
        AppUser user = token.getUser();
        user.setPasswordHash(passwords.encode(request.password()));
        user.invalidateSessions();
        token.markUsed();
        tokens.invalidateActiveForUser(user.getId(), Instant.now());
        audit.record(user, "PASSWORD_RESET_COMPLETED", "USER", user.getId(), "Existing sessions invalidated");
    }
    static String sha256(String input) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
    private ApiException invalidToken() { return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", "This password-reset link is invalid or expired."); }
}
