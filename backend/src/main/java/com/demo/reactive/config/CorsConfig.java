package com.demo.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebFluxConfigurer corsConfigurerWebFlux() {
        return new WebFluxConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.reactive.config.CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurerWebMvc() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
