package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.security.LoginDTO;
import com.soaresdev.productorderapi.dtos.security.RefreshDTO;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.repositories.UserRepository;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    private LoginDTO loginDTO;
    private RefreshDTO refreshDTO;
    private TokenDTO tokenDTO;
    private User user;

    private static final String EMAIL = "testing@gmail.com";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldLogin() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtTokenProvider.createToken(anyString(), anyList())).thenReturn(tokenDTO);

        TokenDTO response = authService.login(loginDTO);

        assertNotNull(response);
        assertEquals(loginDTO.getEmail(), response.getEmail());
        assertEquals(loginDTO.getEmail(), user.getEmail());
        assertTrue(response.getAuthenticated());
        assertNotNull(response.getCreation());
        assertEquals(Instant.class, response.getCreation().getClass());
        assertNotNull(response.getExpiration());
        assertEquals(Instant.class, response.getExpiration().getClass());
        assertNotNull(response.getAccessToken());
        assertEquals(String.class, response.getAccessToken().getClass());
        assertFalse(response.getAccessToken().isBlank());
        assertNotNull(response.getRefreshToken());
        assertEquals(String.class, response.getRefreshToken().getClass());
        assertFalse(response.getRefreshToken().isBlank());
        verify(authenticationManager, times(1)).
                authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).createToken(anyString(), anyList());
        verifyNoMoreInteractions(authenticationManager);
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenEmailNotExistsInLogin() {
        when(authenticationManager.
                authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenThrow(InternalAuthenticationServiceException.class);

        Throwable e = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginDTO));
        assertEquals("Invalid email address or password", e.getMessage());
        verify(authenticationManager, times(1)).
                authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenPasswordIsWrongInLogin() {
        when(authenticationManager.
                authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenThrow(BadCredentialsException.class);

        Throwable e = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginDTO));
        assertEquals("Invalid email address or password", e.getMessage());
        verify(authenticationManager, times(1)).
                authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void shouldRefreshToken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailByToken(anyString())).thenReturn(EMAIL);
        when(jwtTokenProvider.createTokenWithRefreshToken(anyString())).thenReturn(tokenDTO);

        TokenDTO response = authService.refreshToken(refreshDTO);

        assertNotNull(response);
        assertEquals(refreshDTO.getEmail(), response.getEmail());
        assertTrue(response.getAuthenticated());
        assertNotNull(response.getCreation());
        assertEquals(Instant.class, response.getCreation().getClass());
        assertNotNull(response.getExpiration());
        assertEquals(Instant.class, response.getExpiration().getClass());
        assertNotNull(response.getAccessToken());
        assertEquals(String.class, response.getAccessToken().getClass());
        assertFalse(response.getAccessToken().isBlank());
        assertNotNull(response.getRefreshToken());
        assertEquals(String.class, response.getRefreshToken().getClass());
        assertFalse(response.getRefreshToken().isBlank());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(jwtTokenProvider, times(1)).getEmailByToken(anyString());
        verify(jwtTokenProvider, times(1)).
                createTokenWithRefreshToken(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenEmailNotExistsInRefreshToken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> authService.refreshToken(refreshDTO));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void shouldThrowAuthenticationServiceExceptionWhenEmailNotMatchInRefreshToken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailByToken(anyString())).thenReturn("differentemail@gmail.com");

        Throwable e = assertThrows(AuthenticationServiceException.class,
                () -> authService.refreshToken(refreshDTO));
        assertEquals("Email not matching", e.getMessage());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(jwtTokenProvider, times(1)).getEmailByToken(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    private void init() {
        loginDTO = new LoginDTO(EMAIL, "testing123");
        refreshDTO = new RefreshDTO(EMAIL, "refresh-token");
        tokenDTO = new TokenDTO(EMAIL, true, Instant.now(), Instant.MAX, "test-access-token", "test-refresh-token");
        user = new User("testing", EMAIL, "testing", "testing123");
    }
}