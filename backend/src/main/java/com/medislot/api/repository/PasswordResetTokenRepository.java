package com.medislot.api.repository;

import com.medislot.api.domain.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    @Query("select count(t) from PasswordResetToken t where t.user.id = :userId and t.createdAt > :after")
    long countByUserIdAndCreatedAtAfter(@Param("userId") UUID userId, @Param("after") Instant after);
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
    void invalidateActiveForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
