package br.com.project.music.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.profile-images-dir}")
    private String uploadDirectory;

    @Value("${file.upload.event-images-dir}")
    private String eventUploadDirectory;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
                .allowedHeaders("*");
                //.allowCredentials(true);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:" + uploadDirectory);

        registry.addResourceHandler("/uploads/event-images/**")
                .addResourceLocations("file:" + eventUploadDirectory);
    }
}