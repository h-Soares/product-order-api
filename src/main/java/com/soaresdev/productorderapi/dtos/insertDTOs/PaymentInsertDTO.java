package com.soaresdev.productorderapi.dtos.insertDTOs;

import com.soaresdev.productorderapi.entities.enums.PaymentType;
import java.io.Serial;
import java.io.Serializable;

public class PaymentInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private PaymentType paymentType;
    private String order_id;

    public PaymentInsertDTO() {
    }

    public PaymentInsertDTO(PaymentType paymentType, String order_id) {
        this.paymentType = paymentType;
        this.order_id = order_id;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }
}