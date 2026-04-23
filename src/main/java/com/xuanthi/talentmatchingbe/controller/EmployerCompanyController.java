package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.service.CompanyService;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employer/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employer Company Controller", description = "Dành cho HR quản lý thông tin công ty mình")
public class EmployerCompanyController {

    private final CompanyService companyService;

    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "HR cập nhật thông tin chi tiết công ty")
    public ResponseEntity<CompanyResponse> updateMyCompany(@Valid @RequestBody CompanyUpdateRequest request) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();
        return ResponseEntity.ok(companyService.updateCompanyByHr(currentUser.getId(), request));
    }

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "HR cập nhật Logo công ty lên Cloudinary")
    public ResponseEntity<String> updateLogo(@RequestParam("file") MultipartFile file) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();
        String logoUrl = companyService.uploadLogo(currentUser.getId(), file);
        return ResponseEntity.ok(logoUrl);
    }
}