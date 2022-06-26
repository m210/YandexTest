package com.example.yandextest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
public class ShopUnitImportRequest {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "parent")
    @JsonManagedReference
    private List<ShopUnitImport> items;

    @NotNull
    private LocalDateTime updateDate;






    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ShopUnitImport> getItems() {
        return items;
    }

    public void setItems(List<ShopUnitImport> items) {
        this.items = items;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "ShopUnitImportRequest{" +
                "id=" + id +
                ", items=" + items +
                ", updateDate=" + updateDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopUnitImportRequest that = (ShopUnitImportRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(items, that.items) && Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, items, updateDate);
    }
}
