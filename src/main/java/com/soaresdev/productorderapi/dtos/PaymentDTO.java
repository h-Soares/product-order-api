package com.soaresdev.productorderapi.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonPropertyOrder({"id", "order_id", "moment", "paymentType", "amount"})
public class PaymentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant moment;
    private PaymentType paymentType;
    private BigDecimal amount;

    private UUID order_id; //order_id is payment_id, if @MapsId

    public PaymentDTO() {
    }

    public PaymentDTO(Instant moment, PaymentType paymentType, BigDecimal amount) {
        this.moment = moment;
        this.paymentType = paymentType;
        this.amount = amount;
    }

    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.moment = payment.getMoment();
        this.paymentType = PaymentType.valueOf(payment.getPaymentType());
        this.amount = payment.getAmount();
        this.order_id = payment.getOrder().getId();
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrder_id() {
        return order_id;
    }

    public void setOrder_id(UUID order_id) {
        this.order_id = order_id;
    }

}