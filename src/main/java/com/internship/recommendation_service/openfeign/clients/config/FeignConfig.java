package com.internship.recommendation_service.openfeign.clients.config;

import com.internship.recommendation_service.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            if (response.status() == 404) {
                return new NotFoundException("Feign resource not found");
            }
            return new Exception("Internal server error");
        }
    }
}