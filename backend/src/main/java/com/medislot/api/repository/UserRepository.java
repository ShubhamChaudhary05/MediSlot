package com.medislot.api.repository;

import com.medislot.api.domain.AppUser;
import java.util.Optional;
import java.util.UUID;
import com.medislot.api.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<AppUser> findByRole(UserRole role, Pageable pageable);
}
