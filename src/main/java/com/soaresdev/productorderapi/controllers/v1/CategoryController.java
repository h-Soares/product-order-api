package com.soaresdev.productorderapi.controllers.v1;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.services.CategoryService;
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
@RequestMapping("/v1/categories")
@Tag(name = "Category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(description = "Get a paginated list of all categories", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> findAll(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }

    @Operation(description = "Get a category by UUID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Illegal argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<CategoryDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(categoryService.findByUUID(uuid));
    }

    @Operation(description = "Insert a new category", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "409", description = "Entity already exists")
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> insert(@RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        CategoryDTO categoryDTO = categoryService.insert(categoryInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(categoryDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryDTO);
    }

    @Operation(description = "Delete a category by UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        categoryService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Update a category by UUID", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "409", description = "Entity already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<CategoryDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        return ResponseEntity.ok(categoryService.updateByUUID(uuid, categoryInsertDTO));
    }
}