package com.soaresdev.productorderapi.dtos;

import com.soaresdev.productorderapi.entities.Order;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class OrderDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant moment;

    private UserDTO client;

    public OrderDTO() {
    }

    public OrderDTO(UUID id, Instant moment, UserDTO client) {
        this.id = id;
        this.moment = moment;
        this.client = client;
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.moment = order.getMoment();
        this.client = new UserDTO(order.getClient());
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

    public UserDTO getClient() {
        return client;
    }

    public void setClient(UserDTO client) {
        this.client = client;
    }
}