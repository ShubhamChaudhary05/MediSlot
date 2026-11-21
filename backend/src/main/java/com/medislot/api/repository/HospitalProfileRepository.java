package com.medislot.api.repository;

import com.medislot.api.domain.HospitalProfile;
import com.medislot.api.domain.ProviderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalProfileRepository extends JpaRepository<HospitalProfile, UUID> {
    Optional<HospitalProfile> findByUserId(UUID userId);
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    Page<HospitalProfile> findByStatus(ProviderStatus status, Pageable pageable);
    @Query(value = "select h from HospitalProfile h where h.status = :status and (:city = '' or lower(h.city) = lower(:city)) and (:q = '' or lower(h.facilityName) like lower(concat('%', :q, '%')))",
           countQuery = "select count(h) from HospitalProfile h where h.status = :status and (:city = '' or lower(h.city) = lower(:city)) and (:q = '' or lower(h.facilityName) like lower(concat('%', :q, '%')))")
    Page<HospitalProfile> searchApproved(@Param("status") ProviderStatus status, @Param("city") String city, @Param("q") String query, Pageable pageable);
}
