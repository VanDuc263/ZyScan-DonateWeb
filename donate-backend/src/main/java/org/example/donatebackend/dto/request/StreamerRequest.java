package org.example.donatebackend.dto.request;

import jakarta.persistence.Column;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public class StreamerRequest {
    private String displayName;
    private String token;
    private String type;
    private MultipartFile file;
    private String thumb;


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }



    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    @Override
    public String toString() {
        return "StreamerRequest{" +
                "displayName='" + displayName + '\'' +
                ", token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", file=" + file +
                ", thumb='" + thumb + '\'' +
                '}';
    }
}
