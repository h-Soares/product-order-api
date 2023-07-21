package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

public class CategoryInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String NAME_REGEX = "^(?!.*[#@!0-9])[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$";

    @NotNull(message = "Name can not be null")
    @Size(min = 1, max = 35, message = "Name must be between 1 and 35 characters")
    @Pattern(regexp = NAME_REGEX, message = "Invalid name")
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