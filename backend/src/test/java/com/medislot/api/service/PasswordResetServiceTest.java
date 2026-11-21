package com.medislot.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.medislot.api.config.AppProperties;
import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.PasswordResetToken;
import com.medislot.api.domain.UserRole;
import com.medislot.api.dto.AuthDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.*;
import com.medislot.api.security.JwtService;
import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordResetServiceTest {
    private UserRepository users;
    private PasswordResetTokenRepository tokens;
    private PasswordResetEmailDelivery delivery;
    private PasswordEncoder encoder;
    private PasswordResetService service;
    private AppUser patient;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        tokens = mock(PasswordResetTokenRepository.class);
        delivery = mock(PasswordResetEmailDelivery.class);
        encoder = new BCryptPasswordEncoder();
        AppProperties properties = new AppProperties();
        properties.getSecurity().setPasswordResetMinutes(15);
        properties.getSecurity().setPasswordResetMaxRequests(3);
        properties.getMail().setResetUrl("https://medislot.test/reset-password");
        patient = new AppUser("patient@example.com", encoder.encode("OldPassword!2026"), "Patient", UserRole.PATIENT);
        service = new PasswordResetService(users, tokens, delivery, properties, encoder, mock(AuditService.class));
    }

    @Test
    void unknownEmailGetsNoTokenOrDelivery() {
        when(users.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        service.request(new AuthDtos.ForgotPasswordRequest("unknown@example.com"));
        verify(tokens, never()).save(any());
        verify(delivery, never()).send(anyString(), any());
    }

    @Test
    void existingEmailCreatesHashedTokenAndDeliversLink() {
        when(users.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(patient));
        when(delivery.isAvailable()).thenReturn(true);
        when(tokens.countByUserIdAndCreatedAtAfter(eq(patient.getId()), any())).thenReturn(0L);
        service.request(new AuthDtos.ForgotPasswordRequest("patient@example.com"));

        ArgumentCaptor<PasswordResetToken> token = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<URI> link = ArgumentCaptor.forClass(URI.class);
        verify(tokens).save(token.capture());
        verify(delivery).send(eq("patient@example.com"), link.capture());
        String rawToken = link.getValue().getQuery().substring("token=".length());
        assertNotEquals(rawToken, token.getValue().getTokenHash());
        assertTrue(token.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void rateLimitedRequestDoesNotCreateOrDeliverAnotherToken() {
        when(users.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(patient));
        when(delivery.isAvailable()).thenReturn(true);
        when(tokens.countByUserIdAndCreatedAtAfter(eq(patient.getId()), any())).thenReturn(3L);

        service.request(new AuthDtos.ForgotPasswordRequest("patient@example.com"));

        verify(tokens, never()).save(any());
        verify(delivery, never()).send(anyString(), any());
    }

    @Test
    void failedDeliveryInvalidatesTheNewToken() {
        when(users.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(patient));
        when(delivery.isAvailable()).thenReturn(true);
        when(tokens.countByUserIdAndCreatedAtAfter(eq(patient.getId()), any())).thenReturn(0L);
        doThrow(new IllegalStateException("mail unavailable")).when(delivery).send(anyString(), any());

        service.request(new AuthDtos.ForgotPasswordRequest("patient@example.com"));

        verify(tokens).save(any(PasswordResetToken.class));
        verify(tokens, times(2)).invalidateActiveForUser(eq(patient.getId()), any());
    }

    @Test
    void validTokenChangesPasswordInvalidatesSessionsAndRejectsReuse() {
        PasswordResetToken token = new PasswordResetToken(patient, "stored-hash", Instant.now().plusSeconds(600));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        AuthDtos.ResetPasswordRequest request = new AuthDtos.ResetPasswordRequest("valid-reset-token-abcdefghijklmnopqrstuvwxyz", "NewPassword!2026");

        service.reset(request);

        assertTrue(encoder.matches("NewPassword!2026", patient.getPasswordHash()));
        assertFalse(encoder.matches("OldPassword!2026", patient.getPasswordHash()));
        assertEquals(1, patient.getAuthVersion());
        assertNotNull(token.getUsedAt());
        verify(tokens).invalidateActiveForUser(eq(patient.getId()), any());
        assertThrows(ApiException.class, () -> service.reset(request));
    }

    @Test
    void expiredAndInvalidTokensAreRejected() {
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(new PasswordResetToken(patient, "expired", Instant.now().minusSeconds(1))));
        assertThrows(ApiException.class, () -> service.reset(new AuthDtos.ResetPasswordRequest("expired-reset-token-abcdefghijklmnopqrstuvwxyz", "NewPassword!2026")));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.reset(new AuthDtos.ResetPasswordRequest("invalid-reset-token-abcdefghijklmnopqrstuvwxyz", "NewPassword!2026")));
    }

    @Test
    void newPasswordCanLogInAndOldPasswordIsRejected() {
        PasswordResetToken token = new PasswordResetToken(patient, "stored-hash", Instant.now().plusSeconds(600));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        service.reset(new AuthDtos.ResetPasswordRequest("login-reset-token-abcdefghijklmnopqrstuvwxyz", "NewPassword!2026"));
        when(users.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(patient));
        AppProperties jwtProperties = new AppProperties();
        jwtProperties.getSecurity().setJwtSecret(Base64.getEncoder().encodeToString(new byte[32]));
        AuthService auth = new AuthService(users, mock(PatientProfileRepository.class), mock(DoctorProfileRepository.class), mock(HospitalProfileRepository.class), encoder, new JwtService(jwtProperties), mock(AuditService.class));
        assertDoesNotThrow(() -> auth.login(new AuthDtos.LoginRequest("patient@example.com", "NewPassword!2026")));
        assertThrows(ApiException.class, () -> auth.login(new AuthDtos.LoginRequest("patient@example.com", "OldPassword!2026")));
    }
}
