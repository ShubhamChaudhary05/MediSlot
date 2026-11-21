package com.medislot.api.repository;

import com.medislot.api.domain.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, java.util.UUID> {
    boolean existsByTokenIdAndExpiresAtAfter(String tokenId, Instant now);
    void deleteByExpiresAtBefore(Instant now);
}
