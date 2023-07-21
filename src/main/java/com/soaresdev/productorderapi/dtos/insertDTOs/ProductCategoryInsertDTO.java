package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

public class ProductCategoryInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    @NotNull(message = "Category uuid can not be null")
    @Pattern(regexp = UUID_REGEX, message = "Invalid category uuid")
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