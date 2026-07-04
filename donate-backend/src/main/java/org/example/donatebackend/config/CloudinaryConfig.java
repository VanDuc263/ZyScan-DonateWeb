package org.example.donatebackend.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String,String> config =  new HashMap<String,String>();

        config.put("cloud_name", "dl6amunu4");
        config.put("api_key", "432247791422967");
        config.put("api_secret", "yFahBGhzufzoTXiA7LesyfVk6Yw");

        return new Cloudinary(config);
    }
}
