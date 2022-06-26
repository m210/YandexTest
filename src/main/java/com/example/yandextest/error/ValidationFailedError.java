package com.example.yandextest.error;

import org.springframework.http.HttpStatus;

public class ValidationFailedError extends Error {

    public ValidationFailedError() {
        super(HttpStatus.BAD_REQUEST, "Validation Failed");
    }
}
