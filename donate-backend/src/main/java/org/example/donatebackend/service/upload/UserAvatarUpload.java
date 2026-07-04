package org.example.donatebackend.service.upload;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@Service
public class UserAvatarUpload implements UploadStrategy {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder","avatar")
            );
        }catch (Exception e){
            e.printStackTrace();
        }

        return Map.of();
    }
}
