package com.soaresdev.productorderapi.dtos.insertDTOs;

import java.io.Serial;
import java.io.Serializable;

public class OrderItemInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer quantity;
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