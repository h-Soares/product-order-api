package com.soaresdev.productorderapi.dtos;

import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class PaymentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant moment;
    private PaymentType paymentType;

    public PaymentDTO() {
    }

    public PaymentDTO(UUID id, Instant moment, PaymentType paymentType) {
        this.id = id;
        this.moment = moment;
        this.paymentType = paymentType;
    }

    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.moment = payment.getMoment();
        this.paymentType = PaymentType.valueOf(payment.getPaymentType());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getMoment() {
        return moment;
    }

    public void setMoment(Instant moment) {
        this.moment = moment;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
}