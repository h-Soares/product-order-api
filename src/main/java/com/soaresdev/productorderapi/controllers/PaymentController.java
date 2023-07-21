package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.PaymentDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.services.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PaymentDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(paymentService.findByUUID(uuid));
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> insert(@RequestBody @Valid PaymentInsertDTO paymentInsertDTO) {
        PaymentDTO paymentDTO = paymentService.insert(paymentInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(paymentDTO.getOrder_id()).toUri();
        return ResponseEntity.created(uri).body(paymentDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        paymentService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PaymentDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid PaymentInsertDTO paymentInsertDTO) {
        return ResponseEntity.ok(paymentService.updateByUUID(uuid, paymentInsertDTO));
    }
}