package com.soaresdev.productorderapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonPropertyOrder({"id","moment", "orderStatus", "total", "client", "payment", "items"})
public class OrderDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant moment;
    private OrderStatus orderStatus;
    private BigDecimal total;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("payment")
    private PaymentDTO paymentDTO;
    private UserDTO client;
    private Set<OrderItemDTO> items = new HashSet<>();

    public OrderDTO() {
    }

    public OrderDTO(UUID id, Instant moment, UserDTO client, OrderStatus orderStatus, PaymentDTO paymentDTO) {
        this.id = id;
        this.moment = moment;
        this.orderStatus = orderStatus;
        this.client = client;
        this.paymentDTO = paymentDTO;
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.moment = order.getMoment();
        this.orderStatus = OrderStatus.valueOf(order.getOrderStatus());
        this.client = new UserDTO(order.getClient());
        this.items = order.getItems().stream().map(OrderItemDTO::new).collect(Collectors.toSet());
        this.total = order.getTotal();
        if(order.getPayment() != null)
            this.paymentDTO = new PaymentDTO(order.getPayment());
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

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public UserDTO getClient() {
        return client;
    }

    public void setClient(UserDTO client) {
        this.client = client;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PaymentDTO getPaymentDTO() {
        return paymentDTO;
    }

    public void setPaymentDTO(PaymentDTO paymentDTO) {
        this.paymentDTO = paymentDTO;
    }

    public void setItems(Set<OrderItemDTO> items) {
        this.items = items;
    }

    public Set<OrderItemDTO> getItems() {
        return items;
    }
}