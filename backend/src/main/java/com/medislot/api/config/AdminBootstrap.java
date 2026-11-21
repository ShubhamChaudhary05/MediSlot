package com.medislot.api.config;

import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.UserRole;
import com.medislot.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final AppProperties properties; private final UserRepository users; private final PasswordEncoder encoder;
    public AdminBootstrap(AppProperties properties, UserRepository users, PasswordEncoder encoder) { this.properties = properties; this.users = users; this.encoder = encoder; }
    @Override @Transactional public void run(ApplicationArguments args) {
        String email = properties.getAdmin().getBootstrapEmail();
        String password = properties.getAdmin().getBootstrapPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        if (password.length() < 12) { log.error("Admin bootstrap was skipped because ADMIN_BOOTSTRAP_PASSWORD is too short."); return; }
        if (users.existsByEmailIgnoreCase(email.trim())) return;
        users.save(new AppUser(email.trim().toLowerCase(java.util.Locale.ROOT), encoder.encode(password), properties.getAdmin().getBootstrapName(), UserRole.ADMIN));
        log.warn("A configured bootstrap administrator was created. Remove ADMIN_BOOTSTRAP_PASSWORD from the environment after first use.");
    }
}
