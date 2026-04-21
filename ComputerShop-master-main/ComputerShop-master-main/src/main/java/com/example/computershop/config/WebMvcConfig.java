package com.example.computershop.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:src/main/resources/static/uploads/",
                        "file:ComputerShop-master-main/ComputerShop-master-main/src/main/resources/static/uploads/",
                        "file:uploads/",
                        "classpath:/static/uploads/"
                );

        registry.addResourceHandler("/assets/images/product/laptop/**")
                .addResourceLocations(
                        toFileResourceLocation(Paths.get("src", "main", "resources", "static",
                                "assets", "images", "product", "laptop")),
                        toFileResourceLocation(Paths.get("ComputerShop-master-main", "ComputerShop-master-main",
                                "src", "main", "resources", "static", "assets", "images", "product", "laptop")),
                        "classpath:/static/assets/images/product/laptop/"
                );
    }

    private String toFileResourceLocation(Path path) {
        return "file:" + path.toAbsolutePath().normalize().toString().replace("\\", "/") + "/";
    }
}
