package com.soaresdev.productorderapi.dtos.insertDTOs;

import java.io.Serial;
import java.io.Serializable;

public class ProductCategoryInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String category_uuid;

    public ProductCategoryInsertDTO() {
    }

    public ProductCategoryInsertDTO(String category_uuid) {
        this.category_uuid = category_uuid;
    }

    public String getCategory_uuid() {
        return category_uuid;
    }

    public void setCategory_uuid(String category_uuid) {
        this.category_uuid = category_uuid;
    }
}