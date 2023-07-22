package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> findAll(@PageableDefault(sort = "client.name") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<OrderDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(orderService.findByUUID(uuid));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> insert(@RequestBody @Valid OrderInsertDTO orderInsertDTO) {
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
    public ResponseEntity<OrderDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid OrderInsertDTO orderInsertDTO) {
        return ResponseEntity.ok().body(orderService.updateByUUID(uuid, orderInsertDTO));
    }

    @PostMapping("/{order_uuid}/items")
    public ResponseEntity<OrderDTO> addItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemInsertDTO orderItemInsertDTO) {
        return ResponseEntity.ok(orderService.addItem(order_uuid, orderItemInsertDTO));
    }

    @DeleteMapping("/{order_uuid}/items")
    public ResponseEntity<OrderDTO> deleteItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemDeleteDTO orderItemDeleteDTO) {
        return ResponseEntity.ok(orderService.deleteItem(order_uuid, orderItemDeleteDTO));
    }

    @PutMapping("/{order_uuid}/items")
    public ResponseEntity<OrderDTO> updateItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemInsertDTO orderItemInsertDTO) {
        return ResponseEntity.ok(orderService.updateItem(order_uuid, orderItemInsertDTO));
    }
}