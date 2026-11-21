package com.medislot.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.medislot.api.config.AppProperties;
import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.UserRole;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void issuesAndParsesRoleBoundToken() {
        AppProperties properties = new AppProperties();
        properties.getSecurity().setJwtSecret(Base64.getEncoder().encodeToString(new byte[32]));
        properties.getSecurity().setAccessTokenMinutes(15);
        AppUser user = new AppUser("patient@example.com", "hash", "Patient Example", UserRole.PATIENT);

        JwtService service = new JwtService(properties);
        JwtService.IssuedToken issued = service.issue(user);
        JwtService.ParsedToken parsed = service.parse(issued.value());

        assertEquals(user.getId(), parsed.userId());
        assertEquals("patient@example.com", parsed.email());
        assertEquals("PATIENT", parsed.role());
        assertEquals(0, parsed.authVersion());
        assertFalse(parsed.tokenId().isBlank());
    }
}
