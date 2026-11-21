package com.medislot.api.controller;

import com.medislot.api.dto.ApiResponse;
import com.medislot.api.dto.ProviderDtos;
import com.medislot.api.security.AuthenticatedUser;
import com.medislot.api.service.ProviderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {
    private final ProviderService providers;
    public HospitalController(ProviderService providers) { this.providers = providers; }
    @GetMapping public ApiResponse<Page<ProviderDtos.HospitalSummary>> search(@RequestParam(required = false) String city, @RequestParam(required = false, name = "q") String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) { return ApiResponse.of(providers.findHospitals(city, query, page, size)); }
    @GetMapping("/{id}") public ApiResponse<ProviderDtos.HospitalDetail> detail(@PathVariable UUID id) { return ApiResponse.of(providers.publicHospital(id)); }
    @GetMapping("/me") @PreAuthorize("hasRole('HOSPITAL')") public ApiResponse<ProviderDtos.HospitalDetail> me(@AuthenticationPrincipal AuthenticatedUser user) { return ApiResponse.of(providers.myHospital(user.id())); }
    @PutMapping("/me") @PreAuthorize("hasRole('HOSPITAL')") public ApiResponse<ProviderDtos.HospitalDetail> update(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ProviderDtos.HospitalUpdateRequest request) { return ApiResponse.of(providers.updateHospital(user.id(), request)); }
}
