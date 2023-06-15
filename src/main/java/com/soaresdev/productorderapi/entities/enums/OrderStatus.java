package com.soaresdev.productorderapi.entities.enums;

public enum OrderStatus {
    WAITING_PAYMENT(1),
    PAID(2),
    SHIPPED(3),
    DELIVERED(4),
    CANCELED(5);

    private final Integer code;
    OrderStatus(Integer code) {
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }

    public static OrderStatus valueOf(Integer code) {
        for(OrderStatus orderStatus : OrderStatus.values()) {
            if(code.equals(orderStatus.getCode()))
                return orderStatus;
        }
        return null; /* TODO: throw exception */
    }
}