package com.example.yandextest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class ShopUnitImport {

    @Id
    private String id;
    @NotNull
    private String name;
    private String parentId;
    private ShopUnitType type;
    private Long price;

    @ManyToOne
    @JoinColumn(name = "request_id")
    @JsonBackReference
    private ShopUnitImportRequest parent;






    public ShopUnitImport() {}

    public ShopUnitType getType() {
        return type;
    }

    public void setType(ShopUnitType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopUnitImport item = (ShopUnitImport) o;
        return Objects.equals(type, item.type) && Objects.equals(name, item.name) && Objects.equals(id, item.id) && Objects.equals(parentId, item.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, id, parentId);
    }

    @Override
    public String toString() {
        return "Item{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", price=" + price +
                '}';
    }
}
