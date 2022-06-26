package com.example.yandextest.model;

import java.util.List;

public class ShopUnit extends ShopUnitStatisticUnit {

    private List<ShopUnit> children;

    public List<ShopUnit> getChildren() {
        return children;
    }

    public void setChildren(List<ShopUnit> children) {
        this.children = children;
    }

}
