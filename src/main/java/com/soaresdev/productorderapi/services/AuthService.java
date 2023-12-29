package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.security.LoginDTO;
import com.soaresdev.productorderapi.dtos.security.RefreshDTO;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.repositories.UserRepository;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    public TokenDTO login(LoginDTO data) {
        try {
            String email = data.getEmail();
            String password = data.getPassword();

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            User user = userRepository.findByEmailWithEagerRoles(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            TokenDTO tokenResponse = jwtTokenProvider.createToken(email, user.getRoleNames());
            return tokenResponse;
        }catch(Exception e) {
            throw new BadCredentialsException("Invalid email address or password");
        }
    }

    public TokenDTO refreshToken(RefreshDTO refreshDTO) {
        String email = refreshDTO.getEmail();
        String refreshToken = refreshDTO.getRefreshToken();

        if(!userRepository.existsByEmail(email))
            throw new EntityNotFoundException("User not found");
        if(!jwtTokenProvider.getEmailByToken(refreshToken).equals(email))
            throw new AuthenticationServiceException("Email not matching");

        TokenDTO tokenResponse = jwtTokenProvider.createTokenWithRefreshToken(refreshToken);
        return tokenResponse;
    }
}