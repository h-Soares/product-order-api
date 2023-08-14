package com.soaresdev.productorderapi.dtos.security;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

public class RefreshDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Email can not be null")
    private String email;

    @NotNull(message = "Refresh token can not be null")
    private String refreshToken;

    public RefreshDTO() {
    }

    public RefreshDTO(String email, String refreshToken) {
        this.email = email;
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}