package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.security.LoginDTO;
import com.soaresdev.productorderapi.dtos.security.RefreshDTO;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.exceptions.StandardError;
import com.soaresdev.productorderapi.exceptions.StandardInsertDTOError;
import com.soaresdev.productorderapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authorization")
public class AuthController {
    @Autowired
    AuthService authService;

    @Operation(description = "Login in server", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid arguments", content = @Content(schema = @Schema(implementation = StandardInsertDTOError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = StandardError.class))),
    })
    @PostMapping(value = "/login", consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        TokenDTO tokenDTO = authService.login(loginDTO);
        return ResponseEntity.ok(tokenDTO);
    }

    @Operation(description = "Refresh access token with refresh token", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid arguments", content = @Content(schema = @Schema(implementation = StandardInsertDTOError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = StandardError.class))),
    })
    @PutMapping(value = "/refresh",consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    public ResponseEntity<TokenDTO> refreshToken(@RequestBody @Valid RefreshDTO refreshDTO) {
        return ResponseEntity.ok(authService.refreshToken(refreshDTO));
    }
}