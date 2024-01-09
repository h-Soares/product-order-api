package com.soaresdev.productorderapi.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductCategoryInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductInsertDTO;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.ProductService;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.comparesEqualTo;
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
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/v1/products";
    private static final String STRING_UUID = "09ef0207-2adc-487f-b063-46622970ee44";

    private ProductDTO validProductDTO;
    private ProductInsertDTO validProductInsertDTO;
    private ProductInsertDTO invalidProductInsertDTO;
    private ProductCategoryInsertDTO validProductCategoryInsertDTO;
    private ProductCategoryInsertDTO invalidProductCategoryInsertDTO;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllProductsAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validProductDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.content.size()", is(1))).
                andExpect(jsonPath("$.content[0].id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.content[0].description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.content[0].price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.content[0].imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.content[0].categories.size()", is(1))).
                andExpect(jsonPath("$.content[0].categories[0].id", is(categoryDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].categories.[0].name", is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindAllProductsAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validProductDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PageImpl/content/content").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/id").
                        string(is(validProductDTO.getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/name")
                        .string(is(validProductDTO.getName()))).
                andExpect(xpath("/PageImpl/content/content/description")
                        .string(is(validProductDTO.getDescription()))).
                andExpect(xpath("/PageImpl/content/content/price")
                        .number(comparesEqualTo(validProductDTO.getPrice().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/imgUrl").
                        string(is(validProductDTO.getImgUrl()))).
                andExpect(xpath("/PageImpl/content/content/categories/categories").
                        nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/categories/categories/id")
                        .string(is(categoryDTO.getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/categories/categories/name")
                        .string(is(categoryDTO.getName())))
                .andDo(print());

        verify(productService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindAllProducts() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH)).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindProductByUUIDAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.findByUUID(anyString())).thenReturn(validProductDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validProductDTO.getId()).
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.categories.size()", is(1))).
                andExpect(jsonPath("$.categories[0].id", is(categoryDTO.getId().toString()))).
                andExpect(jsonPath("$.categories.[0].name", is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldFindProductByUUIDAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.findByUUID(anyString())).thenReturn(validProductDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validProductDTO.getId()).
                accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/ProductDTO/id")
                        .string(is(validProductDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/name")
                        .string(is(validProductDTO.getName()))).
                andExpect(xpath("/ProductDTO/description")
                        .string(is(validProductDTO.getDescription()))).
                andExpect(xpath("/ProductDTO/price")
                        .number(comparesEqualTo(validProductDTO.getPrice().doubleValue()))).
                andExpect(xpath("/ProductDTO/imgUrl")
                        .string(is(validProductDTO.getImgUrl()))).
                andExpect(xpath("/ProductDTO/categories/categories")
                        .nodeCount(is(1))).
                andExpect(xpath("/ProductDTO/categories/categories/id")
                        .string(is(categoryDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/categories/categories/name")
                        .string(is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindProductByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH + "/{uuid}",
                validProductDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInFindProductByUUID() throws Exception {
        String errorMessage = "Product not found";
        when(productService.findByUUID(anyString())).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validProductDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verify(productService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldInsertProductAsJsonAndReturn201WhenIsAuthenticated() throws Exception {
        when(productService.insert(any(ProductInsertDTO.class))).
                thenReturn(validProductDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.categories.size()", is(1))).
                andExpect(jsonPath("$.categories[0].id", is(categoryDTO.getId().toString()))).
                andExpect(jsonPath("$.categories.[0].name", is(categoryDTO.getName()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verify(productService, times(1)).
                insert(any(ProductInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldInsertProductAsXmlAndReturn201WhenIsAuthenticated() throws Exception {
        when(productService.insert(any(ProductInsertDTO.class)))
                .thenReturn(validProductDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/ProductDTO/id")
                        .string(is(validProductDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/name")
                        .string(is(validProductDTO.getName()))).
                andExpect(xpath("/ProductDTO/description")
                        .string(is(validProductDTO.getDescription()))).
                andExpect(xpath("/ProductDTO/price")
                        .number(comparesEqualTo(validProductDTO.getPrice().doubleValue()))).
                andExpect(xpath("/ProductDTO/imgUrl")
                        .string(is(validProductDTO.getImgUrl()))).
                andExpect(xpath("/ProductDTO/categories/categories")
                        .nodeCount(is(1))).
                andExpect(xpath("/ProductDTO/categories/categories/id")
                        .string(is(categoryDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/categories/categories/name")
                        .string(is(categoryDTO.getName()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verify(productService, times(1))
                .insert(any(ProductInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidProductInInsertProduct() throws Exception {
        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidProductInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(4))).
                andExpect(jsonPath("$.errors[0]", is("description: Description can not be null"))).
                andExpect(jsonPath("$.errors[1]", is("imgUrl: Invalid image url"))).
                andExpect(jsonPath("$.errors[2]", is("name: Invalid name"))).
                andExpect(jsonPath("$.errors[3]", is("price: Price must be greater than zero"))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verifyNoInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInInsertProduct() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldDeleteProductByUUIDAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(productService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validProductDTO.getId())).
                andExpect(status().isNoContent()).andDo(print());

        verify(productService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteProductByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{uuid}",
                        validProductDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInDeleteProductByUUID() throws Exception {
        String errorMessage = "Product not found";
        doThrow(new EntityNotFoundException(errorMessage)).when(productService).
                deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validProductDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verify(productService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateProductAsJsonByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.updateByUUID(anyString(), any(ProductInsertDTO.class))).
                thenReturn(validProductDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.categories.size()", is(1))).
                andExpect(jsonPath("$.categories[0].id", is(categoryDTO.getId().toString()))).
                andExpect(jsonPath("$.categories.[0].name", is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).
                updateByUUID(anyString(), any(ProductInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateProductAsXmlByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.updateByUUID(anyString(), any(ProductInsertDTO.class))).
                thenReturn(validProductDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/ProductDTO/id")
                        .string(is(validProductDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/name")
                        .string(is(validProductDTO.getName()))).
                andExpect(xpath("/ProductDTO/description")
                        .string(is(validProductDTO.getDescription()))).
                andExpect(xpath("/ProductDTO/price")
                        .number(comparesEqualTo(validProductDTO.getPrice().doubleValue()))).
                andExpect(xpath("/ProductDTO/imgUrl")
                        .string(is(validProductDTO.getImgUrl()))).
                andExpect(xpath("/ProductDTO/categories/categories")
                        .nodeCount(is(1))).
                andExpect(xpath("/ProductDTO/categories/categories/id")
                        .string(is(categoryDTO.getId().toString()))).
                andExpect(xpath("/ProductDTO/categories/categories/name")
                        .string(is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).
                updateByUUID(anyString(), any(ProductInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidProductInUpdateProductByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{uuid}", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidProductInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(4))).
                andExpect(jsonPath("$.errors[0]", is("description: Description can not be null"))).
                andExpect(jsonPath("$.errors[1]", is("imgUrl: Invalid image url"))).
                andExpect(jsonPath("$.errors[2]", is("name: Invalid name"))).
                andExpect(jsonPath("$.errors[3]", is("price: Price must be greater than zero"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verifyNoInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdateProductByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{uuid}",
                        validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidProductInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInUpdateProductByUUID()  throws Exception {
        String errorMessage = "Product not found";
        when(productService.updateByUUID(anyString(), any(ProductInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId()))).
                andDo(print());

        verify(productService, times(1)).
                updateByUUID(anyString(), any(ProductInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldAddCategoryInProductByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(productService.
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenReturn(validProductDTO);

        mvc.perform(post(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.categories.size()", is(1))).
                andExpect(jsonPath("$.categories[0].id", is(categoryDTO.getId().toString()))).
                andExpect(jsonPath("$.categories.[0].name", is(categoryDTO.getName()))).
                andDo(print());

        verify(productService, times(1)).
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidCategoryInAddCategoryInProductByUUID() throws Exception {
        mvc.perform(post(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidProductCategoryInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("category_uuid: Invalid category uuid"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verifyNoInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInAddCategoryInProductByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH + "/{product_uuid}/categories",
                        validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInAddCategoryInProductByUUID() throws Exception {
        String errorMessage = "Category not found";
        when(productService.
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInAddCategoryInProductByUUID() throws Exception {
        String errorMessage = "Product not found";
        when(productService.
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn409WhenCategoryExistsInProductInAddCategoryInProductByUUID() throws Exception {
        String errorMessage = "Category already exists in this product";
        when(productService.
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityExistsException(errorMessage));

        mvc.perform(post(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isConflict()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityExistsException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                addCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldRemoveCategoryFromProductByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        validProductDTO.getCategories().remove(categoryDTO);
        when(productService.
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenReturn(validProductDTO);

        mvc.perform(delete(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.id", is(validProductDTO.getId().toString()))).
                andExpect(jsonPath("$.name", is(validProductDTO.getName()))).
                andExpect(jsonPath("$.description", is(validProductDTO.getDescription()))).
                andExpect(jsonPath("$.price", comparesEqualTo(validProductDTO.getPrice().intValue()))).
                andExpect(jsonPath("$.imgUrl", is(validProductDTO.getImgUrl()))).
                andExpect(jsonPath("$.categories.size()", is(0))).
                andDo(print());

        verify(productService, times(1)).
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidCategoryInRemoveCategoryFromProductByUUID() throws Exception {
        mvc.perform(delete(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidProductCategoryInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("category_uuid: Invalid category uuid"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verifyNoInteractions(productService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInRemoveCategoryFromProductByUUID()  throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{product_uuid}/categories",
                        validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInRemoveCategoryFromProductByUUID() throws Exception {
        String errorMessage = "Category not found";
        when(productService.
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInRemoveCategoryFromProductByUUID() throws Exception {
        String errorMessage = "Product not found";
        when(productService.
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndCategoryNotFoundInProductInRemoveCategoryFromProductByUUID() throws Exception {
        String errorMessage = "Category not found in this product";
        when(productService.
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{product_uuid}/categories", validProductDTO.getId()).
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(validProductCategoryInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validProductDTO.getId() + "/categories"))).
                andDo(print());

        verify(productService, times(1)).
                removeCategoryByUUID(anyString(), any(ProductCategoryInsertDTO.class));
        verifyNoMoreInteractions(productService);
    }

    private void init() {
        categoryDTO = new CategoryDTO("Testing", UUID.fromString(STRING_UUID));
        validProductDTO = new ProductDTO(UUID.fromString(STRING_UUID), "Testing", "Testing", BigDecimal.ONE, "https://testing.com");
        validProductDTO.getCategories().add(categoryDTO);
        validProductInsertDTO = new ProductInsertDTO("Testing", "Testing", BigDecimal.ONE, "https://testing.com");
        invalidProductInsertDTO = new ProductInsertDTO("-Testing", null, BigDecimal.ZERO, "testing");
        validProductCategoryInsertDTO = new ProductCategoryInsertDTO(STRING_UUID);
        invalidProductCategoryInsertDTO = new ProductCategoryInsertDTO("invalid-uuid");
    }
}