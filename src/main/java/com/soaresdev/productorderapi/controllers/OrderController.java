package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<OrderDTO> insert(@RequestBody OrderInsertDTO orderInsertDTO) {
        OrderDTO orderDTO = orderService.insert(orderInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(orderDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(orderDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        orderService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<OrderDTO> updateByUUID(@PathVariable String uuid, @RequestBody OrderInsertDTO orderInsertDTO) {
        return ResponseEntity.ok().body(orderService.updateByUUID(uuid, orderInsertDTO));
    }
}