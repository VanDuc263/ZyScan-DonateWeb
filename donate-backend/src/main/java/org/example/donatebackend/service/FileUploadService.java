package org.example.donatebackend.service;

import com.cloudinary.Cloudinary;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.service.upload.UploadFactory;
import org.example.donatebackend.service.upload.UploadStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileUploadService {

    @Autowired
    private UploadFactory uploadFactory;

    public String upload(String type,MultipartFile file) {
        try {
            UploadStrategy strategy = uploadFactory.get(type);
            Map res = strategy.upload(file);
            String url = (String) res.get("url");
            return url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
