package com.example.yandextest.repository;

import com.example.yandextest.model.ShopUnitImportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRequestRepository extends JpaRepository<ShopUnitImportRequest, Long>  {

}
