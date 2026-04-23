package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.job.JdGenerateReq;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ProFeature;
import com.xuanthi.talentmatchingbe.service.HrJdAiService;
import com.xuanthi.talentmatchingbe.service.ProService;
import com.xuanthi.talentmatchingbe.service.UserService;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/hr")
@SecurityRequirement(name = "bearerAuth") // Ép Swagger phải gửi Token
@Tag(name = "HR JD Controller", description = "Các API liên quan đến việc HR tạo Job Description bằng AI")
@RequiredArgsConstructor
public class HrJdController {

    private final UserService userService;
    private final ProService proService;
    @Autowired
    private HrJdAiService hrJdAiService;
    @Operation(summary = "AI gợi ý Job Description", description = "Nhận thông tin yêu cầu công việc thô và trả về JD được AI gợi ý")
    @PostMapping("/generate-jd")
    // Đổi kiểu trả về thành ResponseEntity<Object>
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public CompletableFuture<ResponseEntity<Object>> generateJd(@Valid @RequestBody JdGenerateReq req) {

        System.out.println("[HR] Nhận yêu cầu tự động viết JD. Đang gửi sang Python...");

        return hrJdAiService.generateJdAsync(req)
                // THÀNH CÔNG: Ép cục JdAiResponse về Object
                .thenApply(res -> ResponseEntity.ok().body((Object) res))

                // THẤT BẠI: Ép câu thông báo lỗi String về Object
                .exceptionally(ex -> ResponseEntity.internalServerError().body((Object) ex.getMessage()));
    }

    @GetMapping("/{candidateId}/download-cv")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "HR Tải CV bản gốc (Chỉ dành cho PRO)")
    public ResponseEntity<Map<String, String>> downloadOriginalCv(@PathVariable Long candidateId) {

        // Lấy HR đang đăng nhập
        User currentHr = SecurityUtils.getRequiredCurrentUser();

        proService.validateAndLogUsage(currentHr, ProFeature.DOWNLOAD_ORIGINAL_CV);

        String cvUrl = userService.getOriginalCvUrl(candidateId);

        // 3. Trả về cho Frontend
        return ResponseEntity.ok(Map.of(
                "message", "Tải CV thành công!",
                "url", cvUrl
        ));
    }
}