package com.medislot.api.repository;

import com.medislot.api.domain.DoctorProfile;
import com.medislot.api.domain.ProviderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    Optional<DoctorProfile> findByUserId(UUID userId);
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    Page<DoctorProfile> findByStatus(ProviderStatus status, Pageable pageable);
    @Query(value = "select d from DoctorProfile d where d.status = :status and " +
           "(:city = '' or lower(d.city) = lower(:city)) and " +
           "(:q = '' or lower(d.user.displayName) like lower(concat('%', :q, '%')) or lower(coalesce(d.qualification, '')) like lower(concat('%', :q, '%')))",
           countQuery = "select count(d) from DoctorProfile d where d.status = :status and " +
           "(:city = '' or lower(d.city) = lower(:city)) and " +
           "(:q = '' or lower(d.user.displayName) like lower(concat('%', :q, '%')) or lower(coalesce(d.qualification, '')) like lower(concat('%', :q, '%')))")
    Page<DoctorProfile> searchApproved(@Param("status") ProviderStatus status, @Param("city") String city, @Param("q") String query, Pageable pageable);
}
