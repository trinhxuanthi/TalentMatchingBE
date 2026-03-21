package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.cv.CvRequest;
import com.xuanthi.talentmatchingbe.service.CvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@Tag(name = "CvController", description = "Các API liên quan đến CV")
public class CvController {

    private final CvService cvService;

    @Operation(summary = "Upload CV")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCv(Principal principal, @RequestBody CvRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập!");
        }

        if (request.getCvUrl() == null || request.getCvUrl().isEmpty()) {
            return ResponseEntity.badRequest().body("Link CV không hợp lệ");
        }

        cvService.updateCvUrl(principal.getName(), request.getCvUrl());

        return ResponseEntity.ok(Map.of(
                "message", "Lưu link CV thành công!",
                "status", "Đang chờ AI phân tích"
        ));
    }
}