package com.soaresdev.productorderapi.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.PaymentDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.PaymentService;
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
import java.math.BigDecimal;
import java.time.Instant;
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
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/v1/payments";
    private static final String STRING_UUID = "42c06ac7-d8e8-4ccc-a051-6ad9638eafaf";

    private PaymentDTO validPaymentDTO;
    private PaymentInsertDTO validPaymentInsertDTO;
    private PaymentInsertDTO invalidPaymentInsertDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldFindAllPaymentsAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validPaymentDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.content.size()", is(1))).
                andExpect(jsonPath("$.content[0].id",
                        is(validPaymentDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].moment",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.content[0].paymentType",
                        is(validPaymentDTO.getPaymentType().name()))).
                andExpect(jsonPath("$.content[0].amount",
                        comparesEqualTo(validPaymentDTO.getAmount().intValue()))).
                andExpect(jsonPath("$.content[0].order_id",
                        is(validPaymentDTO.getOrder_id().toString()))).
                andDo(print());

        verify(paymentService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldFindAllPaymentsAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validPaymentDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PageImpl/content/content").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/id")
                        .string(validPaymentDTO.getId().toString())).
                andExpect(xpath("/PageImpl/content/content/moment")
                        .string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PageImpl/content/content/paymentType")
                        .string(validPaymentDTO.getPaymentType().name()))
                .andExpect(xpath("/PageImpl/content/content/amount").
                        number(comparesEqualTo(validPaymentDTO.getAmount().doubleValue())))
                .andExpect(xpath("/PageImpl/content/content/order_id")
                        .string(validPaymentDTO.getOrder_id().toString()))
                .andDo(print());

        verify(paymentService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindAllPayments() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH)).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldFindPaymentByUUIDAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.findByUUID(anyString())).thenReturn(validPaymentDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validPaymentDTO.getId().toString()))).
                andExpect(jsonPath("$.moment",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.paymentType",
                        is(validPaymentDTO.getPaymentType().name()))).
                andExpect(jsonPath("$.amount",
                        comparesEqualTo(validPaymentDTO.getAmount().intValue()))).
                andExpect(jsonPath("$.order_id",
                        is(validPaymentDTO.getOrder_id().toString()))).
                andDo(print());

        verify(paymentService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldFindPaymentByUUIDAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.findByUUID(anyString())).thenReturn(validPaymentDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PaymentDTO/id")
                        .string(validPaymentDTO.getId().toString())).
                andExpect(xpath("/PaymentDTO/moment")
                        .string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PaymentDTO/paymentType")
                        .string(validPaymentDTO.getPaymentType().name())).
                andExpect(xpath("/PaymentDTO/amount")
                        .number(comparesEqualTo(validPaymentDTO.getAmount().doubleValue()))).
                andExpect(xpath("/PaymentDTO/order_id")
                        .string(validPaymentDTO.getOrder_id().toString())).
                andDo(print());

        verify(paymentService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminOrManagerAndDifferentUsersInFindPaymentByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(paymentService.findByUUID(anyString())).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validPaymentDTO.getId())).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindPaymentByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH + "/{uuid}",
                        validPaymentDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndPaymentNotFoundInFindPaymentByUUID() throws Exception {
        String errorMessage = "Payment not found";
        when(paymentService.findByUUID(anyString())).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validPaymentDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldInsertPaymentAsJsonAndReturn201WhenIsAuthenticated() throws Exception {
        when(paymentService.insert(any(PaymentInsertDTO.class))).
                thenReturn(validPaymentDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validPaymentDTO.getId().toString()))).
                andExpect(jsonPath("$.moment",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.paymentType",
                        is(validPaymentDTO.getPaymentType().name()))).
                andExpect(jsonPath("$.amount",
                        comparesEqualTo(validPaymentDTO.getAmount().intValue()))).
                andExpect(jsonPath("$.order_id",
                        is(validPaymentDTO.getOrder_id().toString()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validPaymentDTO.getOrder_id()))).
                andDo(print());

        verify(paymentService, times(1)).
                insert(any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldInsertPaymentAsXmlAndReturn201WhenIsAuthenticated() throws Exception {
        when(paymentService.insert(any(PaymentInsertDTO.class)))
                .thenReturn(validPaymentDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PaymentDTO/id")
                        .string(validPaymentDTO.getId().toString())).
                andExpect(xpath("/PaymentDTO/moment")
                        .string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PaymentDTO/paymentType")
                        .string(validPaymentDTO.getPaymentType().name())).
                andExpect(xpath("/PaymentDTO/amount")
                        .number(comparesEqualTo(validPaymentDTO.getAmount().doubleValue()))).
                andExpect(xpath("/PaymentDTO/order_id")
                        .string(validPaymentDTO.getOrder_id().toString())).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validPaymentDTO.getOrder_id()))).
                andDo(print());

        verify(paymentService, times(1))
                .insert(any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidPaymentInInsertPayment() throws Exception {
        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidPaymentInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("order_id: Invalid order uuid"))).
                andExpect(jsonPath("$.errors[1]", is("paymentType: Payment type can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldReturn403WhenAlreadyPaidInInsertPayment() throws Exception {
        String errorMessage = "Order already paid";
        when(paymentService.insert(any(PaymentInsertDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verify(paymentService, times(1)).
                insert(any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminOrManagerAndDifferentUsersInInsertPayment() throws Exception {
        String errorMessage = "Access denied";
        when(paymentService.insert(any(PaymentInsertDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verify(paymentService, times(1)).
                insert(any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInInsertPayment() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndPaymentOrderNotFoundInInsertPayment() throws Exception {
        String errorMessage = "Order not found";
        when(paymentService.insert(any(PaymentInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verify(paymentService, times(1)).
                insert(any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldDeletePaymentByUUIDAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(paymentService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validPaymentDTO.getId())).
                andExpect(status().isNoContent()).andDo(print());

        verify(paymentService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeletePaymentByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{uuid}",
                        validPaymentDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndPaymentNotFoundInDeletePaymentByUUID()  throws Exception {
        String errorMessage = "Payment not found";
        doThrow(new EntityNotFoundException(errorMessage)).
                when(paymentService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validPaymentDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdatePaymentAsJsonByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.updateByUUID(anyString(), any(PaymentInsertDTO.class))).
                thenReturn(validPaymentDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validPaymentDTO.getId().toString()))).
                andExpect(jsonPath("$.moment",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.paymentType",
                        is(validPaymentDTO.getPaymentType().name()))).
                andExpect(jsonPath("$.amount",
                        comparesEqualTo(validPaymentDTO.getAmount().intValue()))).
                andExpect(jsonPath("$.order_id",
                        is(validPaymentDTO.getOrder_id().toString()))).
                andDo(print());

        verify(paymentService, times(1)).
                updateByUUID(anyString(), any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdatePaymentAsXmlByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(paymentService.updateByUUID(anyString(), any(PaymentInsertDTO.class))).
                thenReturn(validPaymentDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PaymentDTO/id")
                        .string(validPaymentDTO.getId().toString())).
                andExpect(xpath("/PaymentDTO/moment")
                        .string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PaymentDTO/paymentType")
                        .string(validPaymentDTO.getPaymentType().name())).
                andExpect(xpath("/PaymentDTO/amount")
                        .number(comparesEqualTo(validPaymentDTO.getAmount().doubleValue()))).
                andExpect(xpath("/PaymentDTO/order_id")
                        .string(validPaymentDTO.getOrder_id().toString())).
                andDo(print());

        verify(paymentService, times(1))
                .updateByUUID(anyString(), any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidPaymentInUpdatePaymentByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidPaymentInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("order_id: Invalid order uuid"))).
                andExpect(jsonPath("$.errors[1]", is("paymentType: Payment type can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn403WhenAlreadyPaidInUpdatePaymentByUUID() throws Exception {
        String errorMessage = "Order already paid";
        when(paymentService.updateByUUID(anyString(), any(PaymentInsertDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1))
                .updateByUUID(anyString(), any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdatePaymentByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{uuid}",
                        validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndPaymentNotFoundInUpdatePaymentByUUID()throws Exception {
        String errorMessage = "Payment not found";
        when(paymentService.updateByUUID(anyString(), any(PaymentInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1))
                .updateByUUID(anyString(), any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndPaymentOrderNotFoundInUpdatePaymentByUUID()throws Exception {
        String errorMessage = "Order not found";
        when(paymentService.updateByUUID(anyString(), any(PaymentInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validPaymentDTO.getId()).
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(validPaymentInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validPaymentDTO.getId()))).
                andDo(print());

        verify(paymentService, times(1))
                .updateByUUID(anyString(), any(PaymentInsertDTO.class));
        verifyNoMoreInteractions(paymentService);
    }

    private void init() {
        validPaymentDTO = new PaymentDTO(Instant.now(), PaymentType.PIX, BigDecimal.ONE);
        validPaymentDTO.setId(UUID.fromString(STRING_UUID));
        validPaymentDTO.setOrder_id(UUID.fromString(STRING_UUID));
        validPaymentInsertDTO = new PaymentInsertDTO(PaymentType.PIX, STRING_UUID);
        invalidPaymentInsertDTO = new PaymentInsertDTO(null, "invalid-uuid");
    }
}