package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductCategoryInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductInsertDTO;
import com.soaresdev.productorderapi.services.ProductService;
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
@RequestMapping("/products")
@Tag(name = "Product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(description = "Get a paginated list of all products", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @Operation(description = "Get a product by UUID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Illegal argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<ProductDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(productService.findByUUID(uuid));
    }

    @Operation(description = "Insert a new product", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> insert(@RequestBody @Valid ProductInsertDTO productInsertDTO) {
        ProductDTO productDTO = productService.insert(productInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(productDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(productDTO);
    }

    @Operation(description = "Delete a product by UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        productService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Update a product by UUID", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<ProductDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid ProductInsertDTO productInsertDTO) {
        return ResponseEntity.ok(productService.updateByUUID(uuid, productInsertDTO));
    }

    @Operation(description = "Insert a category into a product by product UUID", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "409", description = "Entity already exists")
    })
    @PostMapping("/{product_uuid}/categories")
    public ResponseEntity<ProductDTO> addCategoryByUUID(@PathVariable String product_uuid, @RequestBody @Valid ProductCategoryInsertDTO productCategoryInsertDTO) {
        return ResponseEntity.ok(productService.addCategoryByUUID(product_uuid, productCategoryInsertDTO));
    }

    @Operation(description = "Delete a category from a product by product UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{product_uuid}/categories")
    public ResponseEntity<ProductDTO> removeCategoryByUUID(@PathVariable String product_uuid, @RequestBody @Valid ProductCategoryInsertDTO productCategoryInsertDTO) {
        return ResponseEntity.ok(productService.removeCategoryByUUID(product_uuid, productCategoryInsertDTO));
    }
}