package com.xuanthi.talentmatchingbe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadService {
    private final Cloudinary cloudinary;

    public String uploadAvatar(MultipartFile file) throws IOException {
        //Tự động nén ảnh và đưa vào thư mục 'avatars' trên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "avatars",
                        "resource_type", "auto"
                ));
        return uploadResult.get("url").toString(); // Trả về link ảnh
    }
}
