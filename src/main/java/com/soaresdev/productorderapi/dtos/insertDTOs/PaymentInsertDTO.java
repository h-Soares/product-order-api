package com.soaresdev.productorderapi.dtos.insertDTOs;

import com.soaresdev.productorderapi.entities.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;

public class PaymentInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    @NotNull(message = "Payment type can not be null")
    private PaymentType paymentType;

    @NotNull(message = "Order uuid can not be null")
    @Pattern(regexp = UUID_REGEX, message = "Invalid order uuid")
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