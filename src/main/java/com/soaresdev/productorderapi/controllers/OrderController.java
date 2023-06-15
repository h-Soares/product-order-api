package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<OrderDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(orderService.findByUUID(uuid));
    }
}