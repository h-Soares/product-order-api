package com.soaresdev.productorderapi.dtos;

import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import java.io.Serial;
import java.io.Serializable;

public class OrderInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //TODO: Validate ENUM
    private OrderStatus orderStatus;
    private String client_id;

    public OrderInsertDTO() {
    }

    public OrderInsertDTO(OrderStatus orderStatus, String client_id) {
        this.orderStatus = orderStatus;
        this.client_id = client_id;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
}