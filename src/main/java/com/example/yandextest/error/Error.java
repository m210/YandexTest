package com.example.yandextest.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Error<T> extends ResponseEntity {
    private HttpStatus code;

    public Error(HttpStatus code, T body) {
        super(body, code);
    }

    public HttpStatus getCode() {
        return code;
    }
}
