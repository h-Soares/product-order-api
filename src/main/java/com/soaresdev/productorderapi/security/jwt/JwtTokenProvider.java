package com.soaresdev.productorderapi.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class JwtTokenProvider {
    private static String SECRET_KEY;

    private final UserDetailsService userDetailsService;
    private final Clock clock;
    private Algorithm algorithm;

    public JwtTokenProvider(UserDetailsService userDetailsService, Clock clock) {
        this.userDetailsService = userDetailsService;
        this.clock = clock;
    }

    @PostConstruct
    protected void init() {
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
        algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
    }

    public TokenDTO createToken(String email, List<String> roles) {
        Instant creation = Instant.now(clock);
        Instant expiration = creation.plus(Duration.ofHours(1));
        String accessToken = getAccessToken(email, roles, creation, expiration);
        String refreshToken = getRefreshToken(email, roles, creation);
        return new TokenDTO(email, true, creation, expiration, accessToken, refreshToken);
    }

    public TokenDTO createTokenWithRefreshToken(String refreshToken) {
            if(!isRefreshToken(refreshToken))
                throw new InvalidClaimException("Invalid or expired token");

            DecodedJWT decodedJWT = verifyAndDecodeToken(refreshToken);
            String email = decodedJWT.getSubject();
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
            return createToken(email,roles);
    }

    private String getAccessToken(String email, List<String> roles, Instant creation, Instant expiration) {
        try {
            String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return JWT.create()
                    .withClaim("roles", roles)
                    .withClaim("purpose", "access")
                    .withIssuedAt(creation)
                    .withExpiresAt(expiration)
                    .withSubject(email)
                    .withIssuer(issuerUrl)
                    .sign(algorithm)
                    .strip();
        }catch(JWTVerificationException e) {
            throw new JWTCreationException("Error generating access token", new Throwable("Algorithm error"));
        }
    }

    private String getRefreshToken(String email, List<String> roles, Instant creation) {
        try {
            Instant refreshTokenExpiration = creation.plus(Duration.ofHours(3));
            return JWT.create()
                    .withClaim("roles", roles)
                    .withClaim("purpose", "refresh")
                    .withIssuedAt(creation)
                    .withExpiresAt(refreshTokenExpiration)
                    .withSubject(email)
                    .sign(algorithm)
                    .strip();
        }catch(JWTVerificationException e) {
            throw new JWTCreationException("Error generating refresh token", new Throwable("Algorithm error"));
        }
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = verifyAndDecodeToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(decodedJWT.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT verifyAndDecodeToken(String token) {
        try {
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            return decodedJWT;
        }catch(JWTVerificationException e) {
            throw new JWTDecodeException("Error decoding token: Invalid or expired token");
        }
    }

    public String fixRequestTokenFormat(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer "))
            return bearerToken.replace("Bearer ", "");
        return null;
    }

    public boolean isAccessToken(String token) {
        DecodedJWT decodedJWT = verifyAndDecodeToken(token);
        return decodedJWT.getClaims().containsKey("purpose") &&
               decodedJWT.getClaims().get("purpose").asString().equals("access");
    }

    public boolean isRefreshToken(String token) {
        DecodedJWT decodedJWT = verifyAndDecodeToken(token);
        return decodedJWT.getClaims().containsKey("purpose") &&
               decodedJWT.getClaims().get("purpose").asString().equals("refresh");
    }

    public String getEmailByToken(String token) {
        DecodedJWT decodedJWT = verifyAndDecodeToken(token);
        return decodedJWT.getSubject();
    }

    @Value("${security.jwt.token.secret-key}")
    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
    }
}