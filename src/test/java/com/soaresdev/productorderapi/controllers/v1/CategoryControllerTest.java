package com.soaresdev.productorderapi.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.CategoryService;
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
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/v1/categories";

    private CategoryDTO validCategoryDTO;
    private CategoryInsertDTO validCategoryInsertDTO;
    private CategoryInsertDTO invalidCategoryInsertDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllCategoriesAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validCategoryDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.content.size()", is(1))).
                andExpect(jsonPath("$.content[0].id",
                        is(validCategoryDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].name", is(validCategoryDTO.getName()))).
                andDo(print());

        verify(categoryService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllCategoriesAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validCategoryDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PageImpl/content/content").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/id")
                        .string(validCategoryDTO.getId().toString())).
                andExpect(xpath("/PageImpl/content/content/name")
                        .string(validCategoryDTO.getName())).
                andDo(print());

        verify(categoryService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindAllCategories() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH)).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindCategoryByUUIDAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.findByUUID(anyString())).thenReturn(validCategoryDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id",
                        is(validCategoryDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validCategoryDTO.getName()))).
                andDo(print());

        verify(categoryService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindCategoryByUUIDAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.findByUUID(anyString())).thenReturn(validCategoryDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/CategoryDTO/id")
                        .string(validCategoryDTO.getId().toString())).
                andExpect(xpath("/CategoryDTO/name")
                        .string(validCategoryDTO.getName())).
                andDo(print());

        verify(categoryService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindCategoryByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH + "/{uuid}",
                        validCategoryDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInFindCategoryByUUID() throws Exception {
        String errorMessage = "Category not found";
        when(categoryService.findByUUID(anyString())).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validCategoryDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldInsertCategoryAsJsonAndReturn201WhenIsAuthenticated() throws Exception {
        when(categoryService.insert(any(CategoryInsertDTO.class))).
                thenReturn(validCategoryDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id",
                        is(validCategoryDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validCategoryDTO.getName()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).
                insert(any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldInsertCategoryAsXmlAndReturn201WhenIsAuthenticated() throws Exception {
        when(categoryService.insert(any(CategoryInsertDTO.class))).
                thenReturn(validCategoryDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/CategoryDTO/id")
                        .string(validCategoryDTO.getId().toString())).
                andExpect(xpath("/CategoryDTO/name")
                        .string(validCategoryDTO.getName())).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).
                insert(any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidCategoryInInsertCategory() throws Exception {
        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidCategoryInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("name: Invalid name"))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verifyNoInteractions(categoryService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInInsertCategory()  throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn409WhenCategoryExistsInInsertCategory() throws Exception {
        String errorMessage = "Category already exists";
        when(categoryService.insert(any(CategoryInsertDTO.class))).
                thenThrow(new EntityExistsException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andDo(print());

        verify(categoryService, times(1)).
                insert(any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldDeleteCategoryByUUIDAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(categoryService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validCategoryDTO.getId())).
                andExpect(status().isNoContent()).andDo(print());

        verify(categoryService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteCategoryByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{uuid}",
                        validCategoryDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInDeleteCategoryByUUID() throws Exception {
        String errorMessage = "Category not found";
        doThrow(new EntityNotFoundException(errorMessage)).
                when(categoryService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validCategoryDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateCategoryAsJsonByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.updateByUUID(anyString(), any(CategoryInsertDTO.class))).
                thenReturn(validCategoryDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id",
                        is(validCategoryDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validCategoryDTO.getName()))).
                andDo(print());

        verify(categoryService, times(1)).
                updateByUUID(anyString(), any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateCategoryAsXmlByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(categoryService.updateByUUID(anyString(), any(CategoryInsertDTO.class))).
                thenReturn(validCategoryDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/CategoryDTO/id").
                        string(validCategoryDTO.getId().toString())).
                andExpect(xpath("/CategoryDTO/name")
                        .string(validCategoryDTO.getName())).
                andDo(print());

        verify(categoryService, times(1)).
                updateByUUID(anyString(), any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidCategoryInUpdateCategoryByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidCategoryInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("name: Invalid name"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verifyNoInteractions(categoryService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdateCategoryByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{uuid}",
                validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInUpdateCategoryByUUID() throws Exception {
        String errorMessage = "Category not found";
        when(categoryService.updateByUUID(anyString(), any(CategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).
                updateByUUID(anyString(), any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn409WhenCategoryExistsInUpdateCategoryByUUID() throws Exception {
        String errorMessage = "Category name already exists";
        when(categoryService.updateByUUID(anyString(), any(CategoryInsertDTO.class))).
                thenThrow(new EntityExistsException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validCategoryDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validCategoryInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validCategoryDTO.getId()))).
                andDo(print());

        verify(categoryService, times(1)).
                updateByUUID(anyString(), any(CategoryInsertDTO.class));
        verifyNoMoreInteractions(categoryService);
    }

    private void init() {
        validCategoryDTO = new CategoryDTO("Testing", UUID.fromString("70bce1f7-b3c6-4206-98ad-779de114c147"));
        validCategoryInsertDTO = new CategoryInsertDTO("Testing");
        invalidCategoryInsertDTO = new CategoryInsertDTO("-Testing");
    }
}