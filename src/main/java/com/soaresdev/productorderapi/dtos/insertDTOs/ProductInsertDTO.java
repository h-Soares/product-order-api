package com.soaresdev.productorderapi.dtos.insertDTOs;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public class ProductInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private BigDecimal price;
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