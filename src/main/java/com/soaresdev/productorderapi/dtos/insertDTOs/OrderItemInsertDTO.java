package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;

public class OrderItemInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    @NotNull(message = "Quantity can not be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Product uuid can not be null")
    @Pattern(regexp = UUID_REGEX, message = "Invalid product uuid")
    private String product_id;

    public OrderItemInsertDTO() {
    }

    public OrderItemInsertDTO(Integer quantity, String product_id) {
        this.quantity = quantity;
        this.product_id = product_id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }
}