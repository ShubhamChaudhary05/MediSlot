package com.medislot.api.security;

import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.UserRole;
import com.medislot.api.repository.RevokedTokenRepository;
import com.medislot.api.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokens;
    private final UserRepository users;
    public JwtAuthenticationFilter(JwtService jwtService, RevokedTokenRepository revokedTokens, UserRepository users) { this.jwtService = jwtService; this.revokedTokens = revokedTokens; this.users = users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) { chain.doFilter(request, response); return; }
        try {
            JwtService.ParsedToken token = jwtService.parse(header.substring(7));
            if (!revokedTokens.existsByTokenIdAndExpiresAtAfter(token.tokenId(), Instant.now())) {
                AppUser user = users.findById(token.userId()).orElse(null);
                if (user != null && user.isEnabled() && user.getRole().name().equals(token.role()) && user.getAuthVersion() == token.authVersion()) {
                    AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException | IllegalArgumentException | IllegalStateException ignored) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }
}
