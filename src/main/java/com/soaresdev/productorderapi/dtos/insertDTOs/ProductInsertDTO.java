package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public class ProductInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String NAME_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9(),. ]+$";
    private static final String URL_REGEX = "^(https?|ftp):\\/\\/[^\\s\\$.?#].[^\\s]*$";

    @NotNull(message = "Name can not be null")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Pattern(regexp = NAME_REGEX, message = "Invalid name")
    private String name;

    @NotNull(message = "Description can not be null")
    private String description;

    @NotNull(message = "Price can not be null")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Image url can not be null")
    @Pattern(regexp = URL_REGEX, message = "Invalid image url")
    private String imgUrl;

    public ProductInsertDTO() {
    }

    public ProductInsertDTO(String name, String description, BigDecimal price, String imgUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}