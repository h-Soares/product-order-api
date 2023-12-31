package com.soaresdev.productorderapi.security.jwt;

import com.auth0.jwt.exceptions.JWTDecodeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtTokenFilterTest {

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    void shouldDoFilterInternal() throws ServletException, IOException {
        Authentication mockAuthentication = mock(Authentication.class);
        when(jwtTokenProvider.fixRequestTokenFormat(any(HttpServletRequest.class))).
                thenReturn("jwt-token");
        when(jwtTokenProvider.isAccessToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(mockAuthentication);

        jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);


        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNotNull(mockResponse.getOutputStream());
        assertNull(mockResponse.getContentType());
        verify(jwtTokenProvider, times(1)).
                fixRequestTokenFormat(any(HttpServletRequest.class));
        verify(jwtTokenProvider, times(1)).isAccessToken(anyString());
        verify(jwtTokenProvider, times(1)).getAuthentication(anyString());
        verify(mockFilterChain, times(1)).
                doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoMoreInteractions(mockFilterChain);
    }

    @Test
    void shouldDoFilterInternalWhenTokenIsNull() throws ServletException, IOException {
        when(jwtTokenProvider.fixRequestTokenFormat(any(HttpServletRequest.class))).
                thenReturn(null);

        jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNotNull(mockResponse.getOutputStream());
        assertNull(mockResponse.getContentType());
        verify(jwtTokenProvider, times(1)).
                fixRequestTokenFormat(any(HttpServletRequest.class));
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoMoreInteractions(mockFilterChain);
    }

    @Test
    void shouldDoFilterInternalWhenIsNotAccessToken() throws ServletException, IOException {
        when(jwtTokenProvider.fixRequestTokenFormat(any(HttpServletRequest.class))).
                thenReturn("jwt-token");
        when(jwtTokenProvider.isAccessToken(anyString())).thenReturn(false);

        jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNotNull(mockResponse.getOutputStream());
        assertNull(mockResponse.getContentType());
        verify(jwtTokenProvider, times(1)).
                fixRequestTokenFormat(any(HttpServletRequest.class));
        verify(jwtTokenProvider, times(1)).isAccessToken(anyString());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoMoreInteractions(mockFilterChain);
    }

    @Test
    void shouldThrowAnyExceptionWhenExceptionOccursInDoFilterInternal() throws ServletException, IOException {
        when(jwtTokenProvider.fixRequestTokenFormat(any(HttpServletRequest.class))).
                thenReturn("jwt-token");
        when(jwtTokenProvider.isAccessToken(anyString())).thenThrow(JWTDecodeException.class);

        jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
        assertNotNull(mockResponse.getOutputStream());
        assertEquals("application/json", mockResponse.getContentType());
        verify(jwtTokenProvider, times(1)).
                fixRequestTokenFormat(any(HttpServletRequest.class));
        verify(jwtTokenProvider, times(1)).isAccessToken(anyString());
        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoInteractions(mockFilterChain);
    }
}