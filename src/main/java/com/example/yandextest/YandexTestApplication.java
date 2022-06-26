package com.example.yandextest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class YandexTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(YandexTestApplication.class, args);
    }

}
