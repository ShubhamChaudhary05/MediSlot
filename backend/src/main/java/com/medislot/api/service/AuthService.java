package com.medislot.api.service;

import com.medislot.api.domain.*;
import com.medislot.api.dto.AuthDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.*;
import com.medislot.api.security.JwtService;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PatientProfileRepository patients;
    private final DoctorProfileRepository doctors;
    private final HospitalProfileRepository hospitals;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final AuditService audit;
    public AuthService(UserRepository users, PatientProfileRepository patients, DoctorProfileRepository doctors, HospitalProfileRepository hospitals, PasswordEncoder passwords, JwtService jwt, AuditService audit) {
        this.users = users; this.patients = patients; this.doctors = doctors; this.hospitals = hospitals; this.passwords = passwords; this.jwt = jwt; this.audit = audit;
    }
    @Transactional
    public AuthDtos.UserSummary register(AuthDtos.RegisterRequest request) {
        if (request.role() == null || request.role() == UserRole.ADMIN) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Choose patient, doctor, or hospital registration.");
        String email = normalizedEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, "EMAIL_IN_USE", "An account with that email already exists.");
        AppUser user = users.save(new AppUser(email, passwords.encode(request.password()), request.displayName().trim(), request.role()));
        switch (request.role()) {
            case PATIENT -> patients.save(new PatientProfile(user));
            case DOCTOR -> {
                require(request.registrationNumber(), "Registration number is required for doctors.");
                if (doctors.existsByRegistrationNumberIgnoreCase(request.registrationNumber().trim())) throw new ApiException(HttpStatus.CONFLICT, "REGISTRATION_IN_USE", "That professional registration number is already registered.");
                doctors.save(new DoctorProfile(user, request.registrationNumber().trim()));
            }
            case HOSPITAL -> {
                require(request.facilityName(), "Facility name is required for hospitals and clinics.");
                require(request.registrationNumber(), "Registration number is required for hospitals and clinics.");
                if (hospitals.existsByRegistrationNumberIgnoreCase(request.registrationNumber().trim())) throw new ApiException(HttpStatus.CONFLICT, "REGISTRATION_IN_USE", "That facility registration number is already registered.");
                hospitals.save(new HospitalProfile(user, request.facilityName().trim(), request.registrationNumber().trim()));
            }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Unsupported account type.");
        }
        audit.record(user, "ACCOUNT_REGISTERED", "USER", user.getId(), "Self-registration");
        return summary(user);
    }
    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(normalizedEmail(request.email())).orElseThrow(() -> invalidCredentials());
        if (!user.isEnabled() || !passwords.matches(request.password(), user.getPasswordHash())) throw invalidCredentials();
        JwtService.IssuedToken token = jwt.issue(user);
        return new AuthDtos.TokenResponse(token.value(), "Bearer", token.expiresAt(), summary(user));
    }
    @Transactional(readOnly = true)
    public AuthDtos.UserSummary currentUser(java.util.UUID userId) {
        return summary(users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.")));
    }
    @Transactional
    public void logout(AppUser user, JwtService.ParsedToken token, RevokedTokenRepository revoked) {
        if (token.expiresAt().isAfter(Instant.now())) revoked.save(new RevokedToken(token.tokenId(), token.expiresAt()));
        audit.record(user, "ACCOUNT_LOGGED_OUT", "USER", user.getId(), "Access token revoked");
    }
    public AppUser requireUser(java.util.UUID userId) { return users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.")); }
    private AuthDtos.UserSummary summary(AppUser user) {
        String status = switch (user.getRole()) {
            case DOCTOR -> doctors.findByUserId(user.getId()).map(d -> d.getStatus().name()).orElse("PENDING");
            case HOSPITAL -> hospitals.findByUserId(user.getId()).map(h -> h.getStatus().name()).orElse("PENDING");
            default -> null;
        };
        return new AuthDtos.UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), status);
    }
    private static void require(String value, String message) { if (value == null || value.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message); }
    private static String normalizedEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private static ApiException invalidCredentials() { return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect."); }
}
