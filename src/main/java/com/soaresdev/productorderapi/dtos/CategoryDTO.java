package com.soaresdev.productorderapi.dtos;

import com.soaresdev.productorderapi.entities.Category;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class CategoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;

    public CategoryDTO() {
    }

    public CategoryDTO(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}