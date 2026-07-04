package org.example.donatebackend.service.upload;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class StreamerAvatarUpload implements UploadStrategy{
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file) throws IOException {
        return cloudinary.uploader().upload(
                file.getBytes(),
                Map.of("folder","streamers/avatar"
                )
        );
    }
}
