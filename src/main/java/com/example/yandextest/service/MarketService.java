package com.example.yandextest.service;

import com.example.yandextest.model.ShopUnitImportRequest;
import com.example.yandextest.model.ShopUnit;
import com.example.yandextest.model.ShopUnitStatisticResponse;

public interface MarketService {

    boolean importBatch(ShopUnitImportRequest batch);

    ShopUnit findNode(String id);

    int deleteNode(String id);

    ShopUnitStatisticResponse sales(String date);

    ShopUnitStatisticResponse statistics(String id, String dateStart, String dateEnd);

}
