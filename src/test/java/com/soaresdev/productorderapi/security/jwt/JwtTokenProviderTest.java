package com.soaresdev.productorderapi.security.jwt;

import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @InjectMocks
    JwtTokenProvider jwtTokenProvider;

    @Mock
    UserDetailsService userDetailsService;

    private static final String EMAIL = "testing@gmail.com";
    private static final List<String> ROLES = Collections.singletonList(RoleName.ROLE_USER.toString());
    private static final Long ACCESS_TOKEN_DURATION_IN_HOURS = 1L;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtTokenProvider.init();
    }

    @Test
    void createToken() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/testing-context");
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        TokenDTO tokenDTO = jwtTokenProvider.createToken(EMAIL, ROLES);

        assertNotNull(tokenDTO);
        assertEquals(EMAIL, tokenDTO.getEmail());
        assertTrue(tokenDTO.getAuthenticated());
        assertNotNull(tokenDTO.getCreation());
        assertEquals(Instant.class, tokenDTO.getCreation().getClass());
        assertNotNull(tokenDTO.getExpiration());
        assertEquals(Instant.class, tokenDTO.getExpiration().getClass());
        assertEquals(ACCESS_TOKEN_DURATION_IN_HOURS, Duration.between(tokenDTO.getCreation(),
                tokenDTO.getExpiration()).toHours());
        assertNotNull(tokenDTO.getAccessToken());
        assertEquals(String.class, tokenDTO.getAccessToken().getClass());
        assertFalse(tokenDTO.getAccessToken().isEmpty());
        assertNotNull(tokenDTO.getRefreshToken());
        assertEquals(String.class, tokenDTO.getRefreshToken().getClass());
        assertFalse(tokenDTO.getRefreshToken().isEmpty());

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createTokenWithRefreshToken() {
    }

    @Test
    void getAuthentication() {
    }

    @Test
    void isValidToken() {
    }

    @Test
    void fixRequestTokenFormat() {
    }

    @Test
    void isAccessToken() {
    }

    @Test
    void isRefreshToken() {
    }
}