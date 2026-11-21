package com.medislot.api.controller;

import com.medislot.api.domain.AppUser;
import com.medislot.api.dto.ApiResponse;
import com.medislot.api.dto.AuthDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.RevokedTokenRepository;
import com.medislot.api.security.AuthenticatedUser;
import com.medislot.api.security.JwtService;
import com.medislot.api.service.AuthService;
import com.medislot.api.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth; private final JwtService jwt; private final RevokedTokenRepository revoked; private final PasswordResetService passwordReset;
    public AuthController(AuthService auth, JwtService jwt, RevokedTokenRepository revoked, PasswordResetService passwordReset) { this.auth = auth; this.jwt = jwt; this.revoked = revoked; this.passwordReset = passwordReset; }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.UserSummary>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(auth.register(request))); }
    @PostMapping("/login") public ApiResponse<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) { return ApiResponse.of(auth.login(request)); }
    @PostMapping({"/forgot-password", "/password-reset/request"})
    public ResponseEntity<ApiResponse<MessageResponse>> requestPasswordReset(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        passwordReset.request(request);
        return ResponseEntity.accepted().body(ApiResponse.of(new MessageResponse("If an account exists, reset instructions will be sent when email delivery is configured.")));
    }
    @PostMapping({"/reset-password", "/password-reset/confirm"})
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        passwordReset.reset(request);
        return ApiResponse.of(new MessageResponse("Your password has been reset. Please sign in again."));
    }
    @GetMapping("/me") public ApiResponse<AuthDtos.UserSummary> me(@AuthenticationPrincipal AuthenticatedUser principal) { return ApiResponse.of(auth.currentUser(principal.id())); }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.");
        AppUser user = auth.requireUser(principal.id()); auth.logout(user, jwt.parse(header.substring(7)), revoked);
        return ResponseEntity.noContent().build();
    }
    public record MessageResponse(String message) { }
}
