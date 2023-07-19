package com.soaresdev.productorderapi.dtos;

import java.io.Serial;
import java.io.Serializable;

public class CategoryInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    public CategoryInsertDTO() {
    }

    public CategoryInsertDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}