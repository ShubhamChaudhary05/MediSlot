package com.medislot.api.repository;

import com.medislot.api.domain.Specialization;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    List<Specialization> findByActiveTrueOrderByNameAsc();
    boolean existsBySlug(String slug);
}
