package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductCategoryInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductInsertDTO;
import com.soaresdev.productorderapi.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ProductDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(productService.findByUUID(uuid));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> insert(@RequestBody @Valid ProductInsertDTO productInsertDTO) {
        ProductDTO productDTO = productService.insert(productInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(productDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(productDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        productService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ProductDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid ProductInsertDTO productInsertDTO) {
        return ResponseEntity.ok(productService.updateByUUID(uuid, productInsertDTO));
    }

    @PostMapping("/{product_uuid}/categories")
    public ResponseEntity<ProductDTO> addCategoryByUUID(@PathVariable String product_uuid, @RequestBody @Valid ProductCategoryInsertDTO productCategoryInsertDTO) {
        return ResponseEntity.ok(productService.addCategoryByUUID(product_uuid, productCategoryInsertDTO));
    }

    @DeleteMapping("/{product_uuid}/categories")
    public ResponseEntity<ProductDTO> removeCategoryByUUID(@PathVariable String product_uuid, @RequestBody @Valid ProductCategoryInsertDTO productCategoryInsertDTO) {
        return ResponseEntity.ok(productService.removeCategoryByUUID(product_uuid, productCategoryInsertDTO));
    }
}