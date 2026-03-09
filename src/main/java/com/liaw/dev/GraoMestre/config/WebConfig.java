package com.liaw.dev.GraoMestre.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Value("${image.base-path}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        // Mapeia o caminho da URL para o local físico das imagens
        registry.addResourceHandler(basePath + "**")
                .addResourceLocations(absolutePath);
    }
}