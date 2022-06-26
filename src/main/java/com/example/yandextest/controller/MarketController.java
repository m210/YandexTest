package com.example.yandextest.controller;

import com.example.yandextest.error.ItemNotFoundError;
import com.example.yandextest.error.ValidationFailedError;
import com.example.yandextest.model.ShopUnitImportRequest;
import com.example.yandextest.model.ShopUnit;
import com.example.yandextest.model.ShopUnitStatisticResponse;
import com.example.yandextest.service.MarketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.yandextest.error.Error;

import java.time.format.DateTimeParseException;

@RestController
public class MarketController {

    private final MarketService service;

    public MarketController(MarketService service) {
        this.service = service;
    }

    @PostMapping("imports")
    public Error imports(@RequestBody ShopUnitImportRequest batch) {
        if(!service.importBatch(batch)) {
            return new ValidationFailedError();
        }
        return new Error(HttpStatus.OK, "The insert or update was successful.");
    }

    @DeleteMapping("delete/{id}")
    public Error delete(@PathVariable String id) {
        int code = service.deleteNode(id);
        switch(code) {
            case 400:
                return new ValidationFailedError();
            case 404:
                return new ItemNotFoundError();
        }

        return new Error(HttpStatus.OK, "The removal was successful.");
    }

    @GetMapping("nodes/{id}")
    public Error<ShopUnit> nodes(@PathVariable String id) {
        ShopUnit node = service.findNode(id);
        if(node == null) {
            return new ItemNotFoundError();
        }
        return new Error(HttpStatus.OK, node);
    }

    @GetMapping("sales")
    public Error<ShopUnitStatisticResponse> sales(@RequestParam String date) {
        ShopUnitStatisticResponse node = null;
        try {
            node = service.sales(date);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return new ValidationFailedError();
        }

        if(node == null) {
            return new ItemNotFoundError();
        }

        return new Error(HttpStatus.OK, node);
    }

    @GetMapping("node/{id}/statistic")
    public Error<ShopUnitStatisticResponse> statistics(@PathVariable String id,
                                                       @RequestParam(required = false) String dateStart,
                                                       @RequestParam(required = false) String dateEnd) {
        ShopUnitStatisticResponse node = null;
        try {
            node = service.statistics(id, dateStart, dateEnd);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return new ValidationFailedError();
        }

        if(node == null) {
            return new ItemNotFoundError();
        }
        return new Error(HttpStatus.OK, node);
    }
}
