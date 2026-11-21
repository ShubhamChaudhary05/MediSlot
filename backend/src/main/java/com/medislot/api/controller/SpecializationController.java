package com.medislot.api.controller;

import com.medislot.api.domain.Specialization;
import com.medislot.api.dto.ApiResponse;
import com.medislot.api.service.SpecializationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {
    private final SpecializationService specializations;
    public SpecializationController(SpecializationService specializations) { this.specializations = specializations; }
    @GetMapping public ApiResponse<List<Specialization>> list() { return ApiResponse.of(specializations.active()); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Specialization> create(@Valid @RequestBody CreateSpecializationRequest request) { return ApiResponse.of(specializations.create(request.name())); }
    public record CreateSpecializationRequest(@NotBlank @Size(max = 120) String name) { }
}
