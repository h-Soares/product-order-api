package com.soaresdev.productorderapi.controllers.v1;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(description = "Get a paginated list of all orders", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping(produces = {"application/json", "application/xml"})
    public ResponseEntity<Page<OrderDTO>> findAll(@PageableDefault(sort = "client.name") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @Operation(description = "Get a order by UUID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Illegal argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping(value = "/{uuid}", produces = {"application/json", "application/xml"})
    public ResponseEntity<OrderDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(orderService.findByUUID(uuid));
    }

    @Operation(description = "Insert a new order", method = "POST", summary = "Order status: WAITING_PAYMENT, PAID, SHIPPED, DELIVERED, CANCELED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "402", description = "Payment required"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PostMapping(consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<OrderDTO> insert(@RequestBody @Valid OrderInsertDTO orderInsertDTO) {
        OrderDTO orderDTO = orderService.insert(orderInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(orderDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(orderDTO);
    }

    @Operation(description = "Delete an order by UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        orderService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Update an order by UUID", method = "PUT", summary = "Order status: WAITING_PAYMENT, PAID, SHIPPED, DELIVERED, CANCELED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "402", description = "Payment required"),
            @ApiResponse(responseCode = "403", description = "Already paid"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(value = "/{uuid}", consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<OrderDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid OrderInsertDTO orderInsertDTO) {
        return ResponseEntity.ok().body(orderService.updateByUUID(uuid, orderInsertDTO));
    }

    @Operation(description = "Insert a new order item by order UUID", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "403", description = "Already paid"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PostMapping(value = "/{order_uuid}/items", consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<OrderDTO> addItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemInsertDTO orderItemInsertDTO) {
        return ResponseEntity.ok(orderService.addItem(order_uuid, orderItemInsertDTO));
    }

    @Operation(description = "Delete an order item by order UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "403", description = "Already paid"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{order_uuid}/items")
    public ResponseEntity<OrderDTO> deleteItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemDeleteDTO orderItemDeleteDTO) {
        return ResponseEntity.ok(orderService.deleteItem(order_uuid, orderItemDeleteDTO));
    }

    @Operation(description = "Update an order item by order UUID", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "403", description = "Already paid"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(value = "/{order_uuid}/items", consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<OrderDTO> updateItemByUUID(@PathVariable String order_uuid, @RequestBody @Valid OrderItemInsertDTO orderItemInsertDTO) {
        return ResponseEntity.ok(orderService.updateItem(order_uuid, orderItemInsertDTO));
    }
}