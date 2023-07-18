package com.soaresdev.productorderapi.entities;

import com.soaresdev.productorderapi.entities.enums.PaymentType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_payment")
public class Payment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    //@GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Instant moment;
    @Column(nullable = false)
    private Integer paymentType; //Maybe remove to make it more automatic

    @OneToOne
    @MapsId
    //@OnDelete(action = OnDeleteAction.CASCADE)
    //@JoinColumn(name = "order_id")
    private Order order;

    public Payment() {
    }

    public Payment(Instant moment, PaymentType paymentType, Order order) {
        this.moment = moment;
        this.paymentType = paymentType.getCode();
        this.order = order;
        this.id = order.getId(); //To have the same id as its associated order.
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

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType.getCode();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}