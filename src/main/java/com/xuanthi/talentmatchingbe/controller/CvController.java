package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.service.CvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvService cvService; // Tên Service của bro có thể khác

    // Đánh dấu consumes = MULTIPART_FORM_DATA_VALUE để báo cho Spring biết: "Hàm này chuyên nhận File"
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }

        // Gọi đúng cái hàm vừa viết
        String fileUrl = cvService.uploadAndUpdateCv(file);

        return ResponseEntity.ok(fileUrl);
    }
}