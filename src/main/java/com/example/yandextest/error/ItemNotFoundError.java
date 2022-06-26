package com.example.yandextest.error;

import org.springframework.http.HttpStatus;

public class ItemNotFoundError extends Error {
    public ItemNotFoundError() {
        super(HttpStatus.NOT_FOUND, "Item not found");
    }
}
