package com.soaresdev.productorderapi.dtos.insertDTOs;

import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

public class OrderInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    @NotNull(message = "Order status can not be null")
    private OrderStatus orderStatus;

    @NotNull(message = "Client uuid can not be null")
    @Pattern(regexp = UUID_REGEX, message = "Invalid client uuid")
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