package org.example.donatebackend.service.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface UploadStrategy {

    Map upload(MultipartFile file) throws IOException;
}
