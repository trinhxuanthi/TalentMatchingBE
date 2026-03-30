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
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        // resource_type = auto để nó tự động nhận diện File PDF, DOCX hay Ảnh
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "auto"
                ));

        // Trả về link ảnh/file PDF đuôi HTTPS bảo mật
        return uploadResult.get("secure_url").toString();
    }
}