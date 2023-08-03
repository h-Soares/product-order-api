package com.soaresdev.productorderapi.dtos.insertDTOs.security;

import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;

public class LoginDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Email can not be null")
    private String email;
    @NotNull(message = "Password can not be null")
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}