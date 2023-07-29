package com.soaresdev.productorderapi.controllers.v1;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.services.UserService;
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
@RequestMapping("/v1/users")
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(description = "Get a paginated list of all users", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(description = "Get an user by UUID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Illegal argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(userService.findByUUID(uuid));
    }

    @Operation(description = "Insert a new user", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "409", description = "Entity already exists")
    })
    @PostMapping
    public ResponseEntity<UserDTO> insert(@RequestBody @Valid UserInsertDTO userInsertDTO) {
        UserDTO userDTO = userService.insert(userInsertDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
                  .buildAndExpand(userDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(userDTO);
    }

    @Operation(description = "Delete an user by UUID", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUUID(@PathVariable String uuid) {
        userService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Update an user by UUID", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid arguments"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "409", description = "Entity already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<UserDTO> updateByUUID(@PathVariable String uuid, @RequestBody @Valid UserInsertDTO userInsertDTO) {
        return ResponseEntity.ok(userService.updateByUUID(uuid, userInsertDTO));
    }
}