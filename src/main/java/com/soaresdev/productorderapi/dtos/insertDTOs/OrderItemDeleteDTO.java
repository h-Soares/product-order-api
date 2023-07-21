package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

public class OrderItemDeleteDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    @NotNull(message = "Product uuid can not be null")
    @Pattern(regexp = UUID_REGEX, message = "Invalid product uuid")
    private String product_id;

    public OrderItemDeleteDTO() {
    }

    public OrderItemDeleteDTO(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }
}