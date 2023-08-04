package com.soaresdev.productorderapi.dtos.security;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class TokenDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String email;
    private Boolean authenticated;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant creation;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant expiration;
    private String accessToken;
    private String refreshToken;

    public TokenDTO() {
    }

    public TokenDTO(String email, Boolean authenticated, Instant creation, Instant expiration, String accessToken, String refreshToken) {
        this.email = email;
        this.authenticated = authenticated;
        this.creation = creation;
        this.expiration = expiration;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Instant getCreation() {
        return creation;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenDTO tokenDTO = (TokenDTO) o;
        return Objects.equals(email, tokenDTO.email) && Objects.equals(authenticated, tokenDTO.authenticated) && Objects.equals(creation, tokenDTO.creation) && Objects.equals(expiration, tokenDTO.expiration) && Objects.equals(accessToken, tokenDTO.accessToken) && Objects.equals(refreshToken, tokenDTO.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, authenticated, creation, expiration, accessToken, refreshToken);
    }
}