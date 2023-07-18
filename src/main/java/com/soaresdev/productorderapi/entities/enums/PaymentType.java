package com.soaresdev.productorderapi.entities.enums;

public enum PaymentType {
    CREDIT_CARD(1),
    PIX(2);

    private final Integer code;
    PaymentType(Integer code) {
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }

    public static PaymentType valueOf(Integer code) {
        for(PaymentType paymentType : PaymentType.values()) {
            if(code.equals(paymentType.getCode()))
                return paymentType;
        }
        throw new IllegalArgumentException("Invalid payment type code");
    }
}