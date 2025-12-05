package com.example.codereview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class CodeReviewBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewBackendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        String frontendOrigin = "http://localhost:3000";
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendOrigin)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
