package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ProductDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(productService.findByUUID(uuid));
    }
}