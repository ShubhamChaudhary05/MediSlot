package com.medislot.api.dto;

import com.medislot.api.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() { }
    public record RegisterRequest(
        @NotBlank @Size(max = 120) String displayName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        UserRole role,
        @Size(max = 180) String facilityName,
        @Size(max = 100) String registrationNumber
    ) { }
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 72) String password) { }
    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt, UserSummary user) { }
    public record UserSummary(UUID id, String email, String displayName, UserRole role, String providerStatus) { }
    public record ForgotPasswordRequest(@NotBlank @Email @Size(max = 254) String email) { }
    public record ResetPasswordRequest(@NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{32,128}$") String token, @NotBlank @Size(min = 12, max = 72) String password) { }
}
