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
        String handlerPath = basePath.endsWith("/") ? basePath + "**" : basePath + "/**";
        
        String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler(handlerPath)
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}