package com.soaresdev.productorderapi.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.soaresdev.productorderapi.security.dtos.TokenDTO;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class JwtTokenProvider {
    private static String SECRET_KEY = System.getenv("SECRET_KEY");

    @Autowired
    UserDetailsService userDetailsService;

    private Algorithm algorithm = null; //TODO: try without null: Algorithm algorithm;

    @PostConstruct
    protected void init() {
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
        algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
    }

    public TokenDTO createToken(String email, List<String> roles) { //TODO: getRoleNames em User
        Instant creation = Instant.now();
        Instant expiration = creation.plus(Duration.ofHours(1)); //mudar para 30s para TESTAR
        String accessToken = getAccessToken(email, roles, creation, expiration);
        String refreshToken = getRefreshToken(email, roles, creation);
        return new TokenDTO(email, true, creation, expiration, accessToken, refreshToken);
    }

    private String getAccessToken(String email, List<String> roles, Instant creation, Instant expiration) {
        try {
            String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return JWT.create()
                    .withClaim("roles", roles)
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
            Instant refreshTokenExpiration = creation.plus(Duration.ofHours(3)); //mudar para 1min para TESTAR
            return JWT.create()
                    .withClaim("roles", roles)
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
        DecodedJWT decodedJWT = decodeToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(decodedJWT.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes()); //TODO: TEST WITHOUT THIS !
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            return decodedJWT;
        }catch(JWTVerificationException e) {
            throw new JWTDecodeException("Error decoding token");
        }
    }

    public boolean isValidToken(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        return decodedJWT.getExpiresAtAsInstant().isAfter(Instant.now());
    }

    public String fixRequestTokenFormat(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer "))
            return bearerToken.replace("Bearer ", "");
        return null;
    }
}