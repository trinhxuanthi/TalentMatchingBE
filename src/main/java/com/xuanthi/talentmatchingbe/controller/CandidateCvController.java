package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.cv.CvGenerateReq;
import com.xuanthi.talentmatchingbe.service.CvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/candidate")
@Tag(name = "Candidate CV Controller", description = "Các API liên quan đến việc ứng viên tạo CV bằng AI")
public class CandidateCvController {

    @Autowired
    private CvService cvService;

    @PostMapping("/generate-cv")
    // Thêm @Valid để nó tự kích hoạt check lỗi trong DTO
    // Trả về CompletableFuture giúp tăng hiệu năng server lên gấp nhiều lần
    @Operation(summary = "AI gợi ý CV chuyên nghiệp", description = "Nhận thông tin kinh nghiệm thô và trả về CV được AI gợi ý")
    public CompletableFuture<ResponseEntity<Object>> generateProfessionalCv(@Valid @RequestBody CvGenerateReq req) {
        System.out.println("Nhận yêu cầu tạo CV từ ứng viên. Đang ném sang AI...");

        return cvService.generateCvAsync(req)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().body(ex.getMessage()));
    }
}