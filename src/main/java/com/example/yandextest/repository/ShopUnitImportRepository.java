package com.example.yandextest.repository;

import com.example.yandextest.model.ShopUnitImportRequest;
import com.example.yandextest.model.ShopUnitImport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopUnitImportRepository extends JpaRepository<ShopUnitImport, String>  {

    int countAllByParent(ShopUnitImportRequest batch);
}
