package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.ai.QuickMatchRequest;
import com.xuanthi.talentmatchingbe.service.QuickMatchAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-core")
@RequiredArgsConstructor
@Tag(name = "AI Core Controller", description = "Cac API tuong tac truc tiep voi Loi AI Python")
public class AiMatchController {

    private final QuickMatchAiService quickMatchAiService;

    @PostMapping("/quick-match")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Check Nong CV bang URL (JSON)", description = "Quet hang loat CV (URL) so voi JD (URL hoac Text).")
    public ResponseEntity<?> triggerAiMatching(@RequestBody QuickMatchRequest request) {

        // LUAT CUNG 1: Khong co CV -> Tra ve loi
        if (request.getCvUrls() == null || request.getCvUrls().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui long cung cap it nhat 1 link CV."));
        }

        boolean hasJdUrl = request.getJdUrl() != null && !request.getJdUrl().trim().isEmpty();
        boolean hasJdText = request.getJdText() != null && !request.getJdText().trim().isEmpty();
        boolean hasJobId = request.getJobId() != null;
        // LUAT CUNG 2: Bat buoc co 1 trong 2
        if (!hasJdUrl && !hasJdText && !hasJobId) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phải cung cấp JD (URL, Text) HOẶC chọn một Job ID có sẵn."));
        }

        try {
            Object result = quickMatchAiService.processAiMatching(
                    request.getCvUrls(),
                    request.getJobId(),
                    request.getJdUrl(),
                    request.getJdText(),
                    request.getCustomRules()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Loi ket noi toi AI Core Server.",
                    "details", e.getMessage()
            ));
        }
    }
}