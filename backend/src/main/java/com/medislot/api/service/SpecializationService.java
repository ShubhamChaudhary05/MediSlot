package com.medislot.api.service;

import com.medislot.api.domain.Specialization;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.SpecializationRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecializationService {
    private final SpecializationRepository specializations;
    public SpecializationService(SpecializationRepository specializations) { this.specializations = specializations; }
    @Transactional(readOnly = true) public List<Specialization> active() { return specializations.findByActiveTrueOrderByNameAsc(); }
    @Transactional public Specialization create(String name) {
        String normalized = name == null ? "" : name.trim();
        String slug = normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (normalized.isBlank() || slug.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "A valid specialization name is required.");
        if (specializations.existsBySlug(slug)) throw new ApiException(HttpStatus.CONFLICT, "SPECIALIZATION_EXISTS", "This specialization already exists.");
        return specializations.save(new Specialization(normalized, slug));
    }
}
