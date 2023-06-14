package com.soaresdev.productorderapi.controllers;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserDTO> findByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(userService.findByUUID(uuid));
    }
}