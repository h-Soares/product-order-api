package com.soaresdev.productorderapi.dtos.insertDTOs;

import java.io.Serial;
import java.io.Serializable;

public class OrderItemDeleteDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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