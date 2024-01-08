package com.soaresdev.productorderapi.controllers;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.security.LoginDTO;
import com.soaresdev.productorderapi.dtos.security.RefreshDTO;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/auth";

    private LoginDTO validLoginDTO;
    private LoginDTO invalidLoginDTO;
    private RefreshDTO validRefreshDTO;
    private RefreshDTO invalidRefreshDTO;
    private TokenDTO tokenDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithAnonymousUser
    void shouldLoginAsJsonAndReturn200() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenReturn(tokenDTO);

        mvc.perform(post(URL_PATH + "/login").contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validLoginDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.email", is(tokenDTO.getEmail()))).
                andExpect(jsonPath("$.authenticated", is(tokenDTO.getAuthenticated()))).
                andExpect(jsonPath("$.creation",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.expiration",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.accessToken", is(tokenDTO.getAccessToken()))).
                andExpect(jsonPath("$.refreshToken", is(tokenDTO.getRefreshToken()))).
                andDo(print());

        verify(authService, times(1)).login(any(LoginDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldLoginAsXmlAndReturn200() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenReturn(tokenDTO);

        mvc.perform(post(URL_PATH + "/login").contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validLoginDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/TokenDTO/email").string(tokenDTO.getEmail())).
                andExpect(xpath("/TokenDTO/authenticated").booleanValue(tokenDTO.getAuthenticated())).
                andExpect(xpath("/TokenDTO/creation").string(
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/TokenDTO/expiration").string(
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/TokenDTO/accessToken").string(tokenDTO.getAccessToken())).
                andExpect(xpath("/TokenDTO/refreshToken").string(tokenDTO.getRefreshToken())).
                andDo(print());

        verify(authService, times(1)).login(any(LoginDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn400WhenSendInvalidLoginInLogin() throws Exception {
        mvc.perform(post(URL_PATH + "/login").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidLoginDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("email: Email can not be null"))).
                andExpect(jsonPath("$.errors[1]", is("password: Password can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/login"))).
                andDo(print());

        verifyNoInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenLoginIsWrongInLogin() throws Exception {
        String errorMessage = "Invalid email address or password";
        when(authService.login(any(LoginDTO.class))).
                thenThrow(new BadCredentialsException(errorMessage));

        mvc.perform(post(URL_PATH + "/login").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validLoginDTO))).
                andExpect(status().isUnauthorized()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.UNAUTHORIZED.value()))).
                andExpect(jsonPath("$.error",
                        is(BadCredentialsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/login"))).
                andDo(print());

        verify(authService, times(1)).login(any(LoginDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldThrow500WhenOccursErrorInTokenCreationInLogin() throws Exception {
        String errorMessage = "Error generating token";
        when(authService.login(any(LoginDTO.class))).
                thenThrow(new JWTCreationException(errorMessage, null));

        mvc.perform(post(URL_PATH + "/login").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validLoginDTO))).
                andExpect(status().isInternalServerError()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value()))).
                andExpect(jsonPath("$.error",
                        is(JWTCreationException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/login"))).
                andDo(print());

        verify(authService, times(1)).login(any(LoginDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldRefreshTokenAsJsonAndReturn200() throws Exception {
        when(authService.refreshToken(any(RefreshDTO.class))).thenReturn(tokenDTO);

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.email", is(tokenDTO.getEmail()))).
                andExpect(jsonPath("$.authenticated", is(tokenDTO.getAuthenticated()))).
                andExpect(jsonPath("$.creation",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.expiration",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.accessToken", is(tokenDTO.getAccessToken()))).
                andExpect(jsonPath("$.refreshToken", is(tokenDTO.getRefreshToken()))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldRefreshTokenAsXmlAndReturn200() throws Exception {
        when(authService.refreshToken(any(RefreshDTO.class))).thenReturn(tokenDTO);

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/TokenDTO/email").string(tokenDTO.getEmail())).
                andExpect(xpath("/TokenDTO/authenticated").booleanValue(tokenDTO.getAuthenticated())).
                andExpect(xpath("/TokenDTO/creation").string(
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/TokenDTO/expiration").string(
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/TokenDTO/accessToken").string(tokenDTO.getAccessToken())).
                andExpect(xpath("/TokenDTO/refreshToken").string(tokenDTO.getRefreshToken())).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn400WhenSendInvalidRefreshTokenInRefreshToken() throws Exception {
        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidRefreshDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("email: Email can not be null"))).
                andExpect(jsonPath("$.errors[1]", is("refreshToken: Refresh token can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verifyNoInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenEmailNotMatchInRefreshToken() throws Exception {
        String errorMessage = "Email not matching";
        when(authService.refreshToken(any(RefreshDTO.class))).
                thenThrow(new AuthenticationServiceException(errorMessage));

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AuthenticationServiceException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotRefreshTokenInRefreshToken() throws Exception {
        String errorMessage = "Invalid or expired token";
        when(authService.refreshToken(any(RefreshDTO.class))).
                thenThrow(new InvalidClaimException(errorMessage));

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(InvalidClaimException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn404WhenUserNotFoundByEmailInRefreshToken() throws Exception {
        String errorMessage = "User not found";
        when(authService.refreshToken(any(RefreshDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn500WhenOccursErrorInTokenDecodeInRefreshToken() throws Exception {
        String errorMessage = "Error decoding token: Invalid or expired token";
        when(authService.refreshToken(any(RefreshDTO.class))).
                thenThrow(new JWTDecodeException(errorMessage));

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isInternalServerError()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value()))).
                andExpect(jsonPath("$.error",
                        is(JWTDecodeException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithAnonymousUser
    void shouldThrow500WhenOccursErrorInTokenCreationInRefreshToken() throws Exception {
        String errorMessage = "Error generating token";
        when(authService.refreshToken(any(RefreshDTO.class))).
                thenThrow(new JWTCreationException(errorMessage, null));

        mvc.perform(put(URL_PATH + "/refresh").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validRefreshDTO))).
                andExpect(status().isInternalServerError()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value()))).
                andExpect(jsonPath("$.error",
                        is(JWTCreationException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/refresh"))).
                andDo(print());

        verify(authService, times(1)).refreshToken(any(RefreshDTO.class));
        verifyNoMoreInteractions(authService);
    }

    private void init() {
        validLoginDTO = new LoginDTO("testing@gmail.com", "testing123");
        invalidLoginDTO = new LoginDTO(null, null);
        validRefreshDTO = new RefreshDTO("testing@gmail.com", "valid-refresh-token");
        invalidRefreshDTO = new RefreshDTO(null, null);
        tokenDTO = new TokenDTO("testing@gmail.com", true,
                Instant.now(), Instant.now().plus(Duration.ofHours(3)),
                "valid-access-token", "valid-refresh-token");
    }
}