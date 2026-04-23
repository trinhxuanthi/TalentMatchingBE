package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JdAiResponse;
import com.xuanthi.talentmatchingbe.dto.job.JdGenerateReq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class HrJdAiService {

    @Value("${python.ai.url:http://localhost:5000/api/quick-match}")
    private String pythonAiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public CompletableFuture<JdAiResponse> generateJdAsync(JdGenerateReq request) {
        return CompletableFuture.supplyAsync(() -> {
            String targetUrl = pythonAiUrl.replace("/api/quick-match", "") + "/api/generate-jd";
            try {
                // Ép thẳng JSON từ Python thành Object chuẩn của Java
                return restTemplate.postForObject(targetUrl, request, JdAiResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("AI đang bận hoặc trả về sai định dạng: " + e.getMessage());
            }
        });
    }
}