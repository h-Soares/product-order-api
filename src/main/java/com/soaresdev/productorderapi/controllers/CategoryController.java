package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CategoryDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(categoryService.findByUUID(uuid));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> insert(@RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        CategoryDTO categoryDTO = categoryService.insert(categoryInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(categoryDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        categoryService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CategoryDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        return ResponseEntity.ok(categoryService.updateByUUID(uuid, categoryInsertDTO));
    }
}