package com.soaresdev.productorderapi.controllers.v1;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.exceptions.StandardError;
import com.soaresdev.productorderapi.exceptions.StandardInsertDTOError;
import com.soaresdev.productorderapi.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Category")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(description = "Get a paginated list of all categories", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping(produces = {"application/json", "application/xml"})
    public ResponseEntity<Page<CategoryDTO>> findAll(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }

    @Operation(description = "Get a category by UUID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Illegal argument", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @GetMapping(value = "/{uuid}", produces = {"application/json", "application/xml"})
    public ResponseEntity<CategoryDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(categoryService.findByUUID(uuid));
    }

    @Operation(description = "Insert a new category", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid arguments", content = @Content(schema = @Schema(implementation = StandardInsertDTOError.class))),
            @ApiResponse(responseCode = "409", description = "Entity already exists", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping(consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<CategoryDTO> insert(@RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        CategoryDTO categoryDTO = categoryService.insert(categoryInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                .buildAndExpand(categoryDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryDTO);
    }

    @Operation(description = "Delete a category by UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid argument", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        categoryService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Update a category by UUID", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid arguments", content = @Content(schema = @Schema(implementation = StandardInsertDTOError.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "409", description = "Entity already exists", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PutMapping(value = "/{uuid}", consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<CategoryDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid CategoryInsertDTO categoryInsertDTO) {
        return ResponseEntity.ok(categoryService.updateByUUID(uuid, categoryInsertDTO));
    }
}