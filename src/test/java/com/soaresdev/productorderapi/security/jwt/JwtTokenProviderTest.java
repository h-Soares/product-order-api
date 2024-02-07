package com.soaresdev.productorderapi.security.jwt;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Clock clock;

    private static final String EMAIL = "testing@gmail.com";
    private static final List<String> ROLES = Collections.singletonList(RoleName.ROLE_USER.toString());
    private static final Long ACCESS_TOKEN_DURATION_IN_HOURS = 1L;
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwicHVycG9zZSI6ImFjY2VzcyIsImlhdCI6MTcwMzgxNDUxNywiZXhwIjozMTU1Njg4OTg0NzEyMzIwMCwic3ViIjoidGVzdGluZ0BnbWFpbC5jb20iLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L3Rlc3RpbmctY29udGV4dCJ9.jdHhbRRiNVKEJ0sx8Uy62Ju16kKpD62EF2cXbL5mlmo";
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;
    private static String EXPIRED_ACCESS_TOKEN;
    private static String EXPIRED_REFRESH_TOKEN;

    @BeforeAll
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtTokenProvider.setSecretKey("secret-key");
        jwtTokenProvider.init();
        ACCESS_TOKEN = createAccessToken();
        REFRESH_TOKEN = createRefreshToken();
        EXPIRED_ACCESS_TOKEN = createExpiredAccessToken();
        EXPIRED_REFRESH_TOKEN = createExpiredRefreshToken();
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(userDetailsService);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @Order(1)
    void shouldCreateToken() {
        mockServletRequestAttributes();

        when(clock.instant()).thenReturn(Instant.now());
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
        assertFalse(tokenDTO.getAccessToken().isBlank());
        assertNotNull(tokenDTO.getRefreshToken());
        assertEquals(String.class, tokenDTO.getRefreshToken().getClass());
        assertFalse(tokenDTO.getRefreshToken().isBlank());
    }

    @Test
    @Order(2)
    void shouldCreateTokenWithRefreshToken() {
        mockServletRequestAttributes();

        TokenDTO newTokenDTO = jwtTokenProvider.createTokenWithRefreshToken(REFRESH_TOKEN);

        assertNotNull(newTokenDTO);
        assertEquals(EMAIL, newTokenDTO.getEmail());
        assertTrue(newTokenDTO.getAuthenticated());
        assertNotNull(newTokenDTO.getCreation());
        assertEquals(Instant.class, newTokenDTO.getCreation().getClass());
        assertNotNull(newTokenDTO.getExpiration());
        assertEquals(Instant.class, newTokenDTO.getExpiration().getClass());
        assertEquals(ACCESS_TOKEN_DURATION_IN_HOURS, Duration.between(newTokenDTO.getCreation(),
                newTokenDTO.getExpiration()).toHours());
        assertNotNull(newTokenDTO.getAccessToken());
        assertEquals(String.class, newTokenDTO.getAccessToken().getClass());
        assertFalse(newTokenDTO.getAccessToken().isBlank());
        assertNotNull(newTokenDTO.getRefreshToken());
        assertEquals(String.class, newTokenDTO.getRefreshToken().getClass());
        assertFalse(newTokenDTO.getRefreshToken().isBlank());
    }

    @Test
    void shouldThrowInvalidClaimExceptionWhenTokenIsNotRefreshTokenInCreateTokenWithRefreshToken() {
        Throwable e = assertThrows(InvalidClaimException.class,
                () -> jwtTokenProvider.createTokenWithRefreshToken(ACCESS_TOKEN));
        assertEquals("Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenRefreshTokenIsExpiredInCreateTokenWithRefreshToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.createTokenWithRefreshToken(EXPIRED_REFRESH_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalidInCreateTokenWithRefreshToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.createTokenWithRefreshToken(INVALID_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldGetAuthentication() {
        UserDetails user = new User("Testing", EMAIL, "testing", "testing");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(user);

        Authentication authentication = jwtTokenProvider.getAuthentication(ACCESS_TOKEN);

        assertNotNull(authentication);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertEquals(user, authentication.getPrincipal());
        assertEquals("", authentication.getCredentials());
        assertEquals(user.getAuthorities(), authentication.getAuthorities());
        assertEquals(user.getUsername(), authentication.getName());
        assertEquals(EMAIL, authentication.getName());
        assertTrue(authentication.isAuthenticated());
        verify(userDetailsService, times(1)).loadUserByUsername(anyString());
        verifyNoMoreInteractions(userDetailsService);
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenAccessTokenIsExpiredInGetAuthentication() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.getAuthentication(EXPIRED_ACCESS_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalidInGetAuthentication() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.getAuthentication(INVALID_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldFixRequestTokenFormat() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ACCESS_TOKEN);

        String token = jwtTokenProvider.fixRequestTokenFormat(request);

        assertNotNull(token);
        assertEquals(ACCESS_TOKEN, token);
    }

    @Test
    void shouldReturnNullWhenAuthorizationInHeaderIsNullInFixRequestTokenFormat() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        String token = jwtTokenProvider.fixRequestTokenFormat(request);

        assertNull(token);
    }

    @Test
    void shouldReturnNullWhenAuthorizationInHeaderNotStartsWithBearerInFixRequestTokenFormat() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(ACCESS_TOKEN);

        String token = jwtTokenProvider.fixRequestTokenFormat(request);

        assertNull(token);
    }

    @Test
    void shouldBeAccessToken() {
        assertTrue(jwtTokenProvider.isAccessToken(ACCESS_TOKEN));
    }

    @Test
    void shouldNotBeAccessToken() {
        assertFalse(jwtTokenProvider.isAccessToken(REFRESH_TOKEN));
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenAccessTokenIsExpiredInIsAccessToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.isAccessToken(EXPIRED_ACCESS_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalidInIsAccessToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.isAccessToken(INVALID_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldBeRefreshToken() {
        assertTrue(jwtTokenProvider.isRefreshToken(REFRESH_TOKEN));
    }

    @Test
    void shouldNotBeRefreshToken() {
        assertFalse(jwtTokenProvider.isRefreshToken(ACCESS_TOKEN));
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenRefreshTokenIsExpiredInIsRefreshToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.isRefreshToken(EXPIRED_REFRESH_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalidInIsRefreshToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.isRefreshToken(INVALID_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldGetEmailByToken() {
        String email = jwtTokenProvider.getEmailByToken(ACCESS_TOKEN);

        assertNotNull(email);
        assertEquals(EMAIL, email);
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenAccessTokenIsExpiredInGetEmailByToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.getEmailByToken(EXPIRED_ACCESS_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalidInGetEmailByToken() {
        Throwable e = assertThrows(JWTDecodeException.class,
                () -> jwtTokenProvider.getEmailByToken(INVALID_TOKEN));
        assertEquals("Error decoding token: Invalid or expired token", e.getMessage());
    }

    private void mockServletRequestAttributes() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/testing-context");
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    private String createAccessToken() {
        mockServletRequestAttributes();
        when(clock.instant()).thenReturn(Instant.now());
        return jwtTokenProvider.createToken(EMAIL, ROLES).getAccessToken();
    }

    private String createRefreshToken() {
        mockServletRequestAttributes();
        when(clock.instant()).thenReturn(Instant.now());
        return jwtTokenProvider.createToken(EMAIL, ROLES).getRefreshToken();
    }

    private String createExpiredAccessToken() {
        mockServletRequestAttributes();
        when(clock.instant()).thenReturn(Instant.MIN);
        return jwtTokenProvider.createToken(EMAIL, ROLES).getAccessToken();
    }

    private String createExpiredRefreshToken() {
        mockServletRequestAttributes();
        when(clock.instant()).thenReturn(Instant.MIN);
        return jwtTokenProvider.createToken(EMAIL, ROLES).getRefreshToken();
    }
}