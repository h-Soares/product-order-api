package com.soaresdev.productorderapi.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserRoleInsertDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/v1/users";

    private User validUser;
    private UserDTO validUserDTO;
    private UserInsertDTO validUserInsertDTO;
    private UserInsertDTO invalidUserInsertDTO;
    private UserRoleInsertDTO validUserRoleInsertDTO;
    private UserRoleInsertDTO invalidUserRoleInsertDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllUsersAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validUserDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.content.size()", is(1))).
                andExpect(jsonPath("$.content[0].id", is(validUserDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].name", is(validUserDTO.getName()))).
                andExpect(jsonPath("$.content[0].email", is(validUserDTO.getEmail()))).
                andExpect(jsonPath("$.content[0].phone", is(validUserDTO.getPhone()))).
                andDo(print());

        verify(userService,times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllUsersAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validUserDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/PageImpl/content/content").nodeCount(is(1)))
                .andExpect(xpath("/PageImpl/content/content/id").
                        string(validUserDTO.getId().toString()))
                .andExpect(xpath("/PageImpl/content/content/name").
                        string(validUserDTO.getName()))
                .andExpect(xpath("/PageImpl/content/content/email").
                        string(validUserDTO.getEmail()))
                .andExpect(xpath("/PageImpl/content/content/phone").
                        string(validUserDTO.getPhone()))
                .andDo(print());

        verify(userService,times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindAllUsers() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH)).
                andExpect(status().isForbidden()).andDo(print()).andReturn();


        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindUserByUUIDAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.findByUUID(anyString())).thenReturn(validUserDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validUser.getId()).
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validUserDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validUserDTO.getName()))).
                andExpect(jsonPath("$.email", is(validUserDTO.getEmail()))).
                andExpect(jsonPath("$.phone", is(validUserDTO.getPhone()))).
                andDo(print());

        verify(userService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindUserByUUIDAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.findByUUID(anyString())).thenReturn(validUserDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validUser.getId()).
                accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/UserDTO/id").string(validUserDTO.getId().toString())).
                andExpect(xpath("/UserDTO/name").string(validUserDTO.getName())).
                andExpect(xpath("/UserDTO/email").string(validUserDTO.getEmail())).
                andExpect(xpath("/UserDTO/phone").string(validUserDTO.getPhone())).
                andDo(print());

        verify(userService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindUserByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH + "/{uuid}", validUser.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInFindUserByUUID() throws Exception {
        String errorMessage = "User not found";
        when(userService.findByUUID(anyString())).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validUser.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldInsertUserAsJsonAndReturn201() throws Exception {
        when(userService.insert(any(UserInsertDTO.class))).thenReturn(validUserDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validUserDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validUserDTO.getName()))).
                andExpect(jsonPath("$.email", is(validUserDTO.getEmail()))).
                andExpect(jsonPath("$.phone", is(validUserDTO.getPhone()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validUserDTO.getId()))).
                andDo(print());

        verify(userService, times(1)).insert(any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldInsertUserAsXmlAndReturn201() throws Exception {
        when(userService.insert(any(UserInsertDTO.class))).thenReturn(validUserDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/UserDTO/id").string(validUserDTO.getId().toString())).
                andExpect(xpath("/UserDTO/name").string(validUserDTO.getName())).
                andExpect(xpath("/UserDTO/email").string(validUserDTO.getEmail())).
                andExpect(xpath("/UserDTO/phone").string(validUserDTO.getPhone())).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validUserDTO.getId()))).
                andDo(print());

        verify(userService, times(1)).insert(any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn400WhenSendInvalidUserInInsertUser() throws Exception {
        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidUserInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(4))).
                andExpect(jsonPath("$.errors[0]", is("email: Invalid email"))).
                andExpect(jsonPath("$.errors[1]", is("name: Invalid name"))).
                andExpect(jsonPath("$.errors[2]", is("password: Password must contain at least 6 characters, with at least one number"))).
                andExpect(jsonPath("$.errors[3]", is("phone: Invalid phone"))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn409WhenEmailExistsInInsertUser() throws Exception {
        String errorMessage = "Email already exists";
        when(userService.insert(any(UserInsertDTO.class))).
                thenThrow(new EntityExistsException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verify(userService, times(1)).insert(any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }


    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldDeleteUserByUUIDAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(userService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validUser.getId())).
                andExpect(status().isNoContent()).andDo(print());

        verify(userService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInDeleteUserByUUID() throws Exception {
        String errorMessage = "Access denied";
        doThrow(new AccessDeniedException(errorMessage)).when(userService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validUser.getId())).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteUserByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "{uuid}", validUser.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInDeleteUserByUUID() throws Exception {
        String errorMessage = "User not found";
        doThrow(new EntityNotFoundException(errorMessage)).when(userService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validUser.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldUpdateUserAsJsonByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.updateByUUID(anyString(), any(UserInsertDTO.class))).
                thenReturn(validUserDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validUserDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validUserDTO.getName()))).
                andExpect(jsonPath("$.email", is(validUserDTO.getEmail()))).
                andExpect(jsonPath("$.phone", is(validUserDTO.getPhone()))).
                andDo(print());

        verify(userService, times(1)).
                updateByUUID(anyString(), any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldUpdateUserAsXmlByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(userService.updateByUUID(anyString(), any(UserInsertDTO.class))).
                thenReturn(validUserDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/UserDTO/id").string(validUserDTO.getId().toString())).
                andExpect(xpath("/UserDTO/name").string(validUserDTO.getName())).
                andExpect(xpath("/UserDTO/email").string(validUserDTO.getEmail())).
                andExpect(xpath("/UserDTO/phone").string(validUserDTO.getPhone())).
                andDo(print());

        verify(userService, times(1)).
                updateByUUID(anyString(), any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn400WhenSendInvalidUserInUpdateUserByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidUserInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(4))).
                andExpect(jsonPath("$.errors[0]", is("email: Invalid email"))).
                andExpect(jsonPath("$.errors[1]", is("name: Invalid name"))).
                andExpect(jsonPath("$.errors[2]", is("password: Password must contain at least 6 characters, with at least one number"))).
                andExpect(jsonPath("$.errors[3]", is("phone: Invalid phone"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInUpdateUserByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(userService.updateByUUID(anyString(), any(UserInsertDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).
                updateByUUID(anyString(), any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdateUserByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInUpdateUserByUUID() throws Exception {
        String errorMessage = "User not found";
        when(userService.updateByUUID(anyString(), any(UserInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).
                updateByUUID(anyString(), any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn409WhenEmailExistsInUpdateUserByUUID() throws Exception {
        String errorMessage = "Email already exists";
        when(userService.updateByUUID(anyString(), any(UserInsertDTO.class))).
                thenThrow(new EntityExistsException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validUser.getId()))).
                andDo(print());

        verify(userService, times(1)).
                updateByUUID(anyString(), any(UserInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddRoleInUserAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(userService).addRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(post(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isNoContent()).andDo(print());

        verify(userService, times(1)).
                addRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenSendInvalidRoleInAddRoleInUser() throws Exception {
        mvc.perform(post(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidUserRoleInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("roleName: Role name can not be null"))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER"})
    void shouldReturn403WhenIsNotAdminInAddRoleInUser() throws Exception {
        mvc.perform(post(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is("Access Denied"))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInAddRoleInUser() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH + "/{uuid}/roles",
                        validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInAddRoleInUser() throws Exception {
        String errorMessage = "User not found";
        doThrow(new EntityNotFoundException(errorMessage)).when(userService).
                addRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(post(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verify(userService, times(1)).
                addRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenRoleExistsInUserInAddRoleInUser() throws Exception {
        String errorMessage = "Role already exists in this user";
        doThrow(new EntityExistsException(errorMessage)).when(userService).
                addRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(post(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verify(userService, times(1)).
                addRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteRoleFromUserAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(userService).deleteRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(delete(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isNoContent()).andDo(print());

        verify(userService, times(1)).
                deleteRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenSendInvalidRoleInDeleteRoleFromUser() throws Exception {
        mvc.perform(delete(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidUserRoleInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("roleName: Role name can not be null"))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER"})
    void shouldReturn403WhenIsNotAdminInDeleteRoleFromUser() throws Exception {
        mvc.perform(delete(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is("Access Denied"))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verifyNoInteractions(userService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteRoleFromUser() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{uuid}/roles",
                        validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInDeleteRoleFromUser() throws Exception {
        String errorMessage = "User not found";
        doThrow(new EntityNotFoundException(errorMessage)).when(userService).
                deleteRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(delete(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verify(userService, times(1)).
                deleteRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenIsAuthenticatedAndRoleNotFoundInUserInDeleteRoleFromUser() throws Exception {
        String errorMessage = "Role not found in this user";
        doThrow(new EntityNotFoundException(errorMessage)).when(userService).
                deleteRole(anyString(), any(UserRoleInsertDTO.class));

        mvc.perform(delete(URL_PATH + "/{uuid}/roles", validUser.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validUserRoleInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path",
                        is(URL_PATH + "/" + validUser.getId() + "/roles"))).
                andDo(print());

        verify(userService, times(1)).
                deleteRole(anyString(), any(UserRoleInsertDTO.class));
        verifyNoMoreInteractions(userService);
    }

    private void init() {
        validUser = new User("testing", "testing@gmail.com", "21487451236", "testing123");
        validUser.setId(UUID.fromString("2722f0f6-1db2-4cce-b6f8-389dc96dc147"));
        validUserDTO = new UserDTO(validUser);
        validUserInsertDTO = new UserInsertDTO("testing", "testing@gmail.com", "21487451236", "testing123");
        invalidUserInsertDTO = new UserInsertDTO("1-testing", "testing", "testing", "123");
        validUserRoleInsertDTO = new UserRoleInsertDTO(RoleName.ROLE_USER);
        invalidUserRoleInsertDTO = new UserRoleInsertDTO(null);
    }
}