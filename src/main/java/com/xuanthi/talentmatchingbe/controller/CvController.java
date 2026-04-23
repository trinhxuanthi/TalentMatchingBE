package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.service.CvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cv Controller", description = "Các API liên quan đến CV upload")
public class CvController {

    private final CvService cvService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload CV file lên Cloudinary")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) {

        log.debug("CV upload request received - file: {}", file.getOriginalFilename());

        // ✅ Fail-fast: Kiểm tra file trống
        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống!");
        }

        // ✅ Gọi service để upload - Mọi lỗi (Cloudinary sập, lỗi định dạng...)
        // để GlobalExceptionHandler lo hết.
        String fileUrl = cvService.uploadAndUpdateCv(file);

        log.info("CV uploaded successfully: {}", fileUrl);
        return ResponseEntity.ok(fileUrl);
    }
}