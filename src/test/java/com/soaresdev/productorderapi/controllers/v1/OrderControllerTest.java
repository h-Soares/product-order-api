package com.soaresdev.productorderapi.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.soaresdev.productorderapi.configs.SecurityConfig;
import com.soaresdev.productorderapi.dtos.*;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.exceptions.NotPaidException;
import com.soaresdev.productorderapi.security.jwt.JwtTokenProvider;
import com.soaresdev.productorderapi.services.OrderService;
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
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URL_PATH = "/v1/orders";
    private static final String STRING_UUID = "31bcfb33-d97d-4aec-b16b-71fda9194b79";

    private PaymentDTO paymentDTO;
    private UserDTO userDTO;
    private CategoryDTO categoryDTO;
    private ProductDTO productDTO;
    private OrderItemDTO orderItemDTO;
    private OrderDTO validOrderDTO;
    private OrderInsertDTO validOrderInsertDTO;
    private OrderInsertDTO invalidOrderInsertDTO;
    private OrderItemInsertDTO validOrderItemInsertDTO;
    private OrderItemInsertDTO invalidOrderItemInsertDTO;
    private OrderItemDeleteDTO validOrderItemDeleteDTO;
    private OrderItemDeleteDTO invalidOrderItemDeleteDTO;

    @BeforeEach
    void setup() {
        init();
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldFindAllOrdersAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(validOrderDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.content.size()", is(1))).
                andExpect(jsonPath("$.content[0].id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.content[0].moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.content[0].orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.content[0].total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.content[0].client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.content[0].client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.content[0].client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.content[0].client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.content[0].payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.content[0].payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.content[0].payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.content[0].payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.content[0].payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.content[0].items.size()", is(1))).
                andExpect(jsonPath("$.content[0].items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.content[0].items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.content[0].items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.content[0].items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.content[0].items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.content[0].items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.content[0].items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.content[0].items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.content[0].items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.content[0].items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.content[0].items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldFindAllOrdersAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validOrderDTO)));

        mvc.perform(get(URL_PATH).accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/PageImpl/content/content").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/id").string(is(validOrderDTO.getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PageImpl/content/content/orderStatus").string(is(validOrderDTO.getOrderStatus().name()))).
                andExpect(xpath("/PageImpl/content/content/total").number(comparesEqualTo(validOrderDTO.getTotal().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/client/id").string(is(validOrderDTO.getClient().getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/client/name").string(is(validOrderDTO.getClient().getName()))).
                andExpect(xpath("/PageImpl/content/content/client/email").string(is(validOrderDTO.getClient().getEmail()))).
                andExpect(xpath("/PageImpl/content/content/client/phone").string(is(validOrderDTO.getClient().getPhone()))).
                andExpect(xpath("/PageImpl/content/content/payment/id").string(is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/payment/order_id").string(is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(xpath("/PageImpl/content/content/payment/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/PageImpl/content/content/payment/paymentType").string(is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(xpath("/PageImpl/content/content/payment/amount").number(comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/items").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/items/items/quantity").string(is(validOrderDTO.getItems().iterator().next().getQuantity().toString()))).
                andExpect(xpath("/PageImpl/content/content/items/items/productPriceRecord").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/items/items/subTotal").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/description").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/price").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().doubleValue()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/imgUrl").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/categories").nodeCount(is(1))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/categories/categories/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(xpath("/PageImpl/content/content/items/items/product/categories/categories/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindAllOrders() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH)).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldFindOrderByUUIDAsJsonAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.findByUUID(anyString())).thenReturn(validOrderDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(1))).
                andExpect(jsonPath("$.items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldFindOrderByUUIDAsXmlAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.findByUUID(anyString())).thenReturn(validOrderDTO);

        mvc.perform(get(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                accept(MediaType.APPLICATION_XML)).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/OrderDTO/id").string(is(validOrderDTO.getId().toString()))).
                andExpect(xpath("/OrderDTO/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/orderStatus").string(is(validOrderDTO.getOrderStatus().name()))).
                andExpect(xpath("/OrderDTO/total").number(comparesEqualTo(validOrderDTO.getTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/client/id").string(is(validOrderDTO.getClient().getId().toString()))).
                andExpect(xpath("/OrderDTO/client/name").string(is(validOrderDTO.getClient().getName()))).
                andExpect(xpath("/OrderDTO/client/email").string(is(validOrderDTO.getClient().getEmail()))).
                andExpect(xpath("/OrderDTO/client/phone").string(is(validOrderDTO.getClient().getPhone()))).
                andExpect(xpath("/OrderDTO/payment/id").string(is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/payment/order_id").string(is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(xpath("/OrderDTO/payment/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/payment/paymentType").string(is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(xpath("/OrderDTO/payment/amount").number(comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/quantity").string(is(validOrderDTO.getItems().iterator().next().getQuantity().toString()))).
                andExpect(xpath("/OrderDTO/items/items/productPriceRecord").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/subTotal").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(xpath("/OrderDTO/items/items/product/description").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(xpath("/OrderDTO/items/items/product/price").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/imgUrl").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminOrManagerAndDifferentUsersInFindOrderByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(orderService.findByUUID(anyString())).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validOrderDTO.getId())).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInFindOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(get(URL_PATH + "/{uuid}",
                        validOrderDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInFindOrderByUUID() throws Exception {
        String errorMessage = "Order not found";
        when(orderService.findByUUID(anyString())).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get(URL_PATH + "/{uuid}", validOrderDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).findByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldInsertOrderAsJsonAndReturn201WhenIsAuthenticated() throws Exception {
        when(orderService.insert(any(OrderInsertDTO.class))).thenReturn(validOrderDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(1))).
                andExpect(jsonPath("$.items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                insert(any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldInsertOrderAsXmlAndReturn201WhenIsAuthenticated() throws Exception {
        when(orderService.insert(any(OrderInsertDTO.class))).thenReturn(validOrderDTO);

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_XML).
                accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isCreated()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/OrderDTO/id").string(is(validOrderDTO.getId().toString()))).
                andExpect(xpath("/OrderDTO/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/orderStatus").string(is(validOrderDTO.getOrderStatus().name()))).
                andExpect(xpath("/OrderDTO/total").number(comparesEqualTo(validOrderDTO.getTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/client/id").string(is(validOrderDTO.getClient().getId().toString()))).
                andExpect(xpath("/OrderDTO/client/name").string(is(validOrderDTO.getClient().getName()))).
                andExpect(xpath("/OrderDTO/client/email").string(is(validOrderDTO.getClient().getEmail()))).
                andExpect(xpath("/OrderDTO/client/phone").string(is(validOrderDTO.getClient().getPhone()))).
                andExpect(xpath("/OrderDTO/payment/id").string(is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/payment/order_id").string(is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(xpath("/OrderDTO/payment/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/payment/paymentType").string(is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(xpath("/OrderDTO/payment/amount").number(comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/quantity").string(is(validOrderDTO.getItems().iterator().next().getQuantity().toString()))).
                andExpect(xpath("/OrderDTO/items/items/productPriceRecord").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/subTotal").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(xpath("/OrderDTO/items/items/product/description").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(xpath("/OrderDTO/items/items/product/price").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/imgUrl").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andExpect(header().exists("Location")).
                andExpect(header().string("Location",
                        containsString(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                insert(any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn400WhenSendInvalidOrderInInsertOrder() throws Exception {
        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidOrderInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("client_id: Invalid client uuid"))).
                andExpect(jsonPath("$.errors[1]", is("orderStatus: Order status can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andExpect(header().doesNotExist("Location")).
                andDo(print());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn402WhenIsAuthenticatedAndInsertPaidOrderInInsertOrder() throws Exception {
        validOrderInsertDTO.setOrderStatus(OrderStatus.PAID);
        String errorMessage = "Not paid yet";
        when(orderService.insert(any(OrderInsertDTO.class))).
                thenThrow(new NotPaidException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isPaymentRequired()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.PAYMENT_REQUIRED.value()))).
                andExpect(jsonPath("$.error",
                        is(NotPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andDo(print());

        verify(orderService, times(1)).
                insert(any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInInsertOrder() throws Exception {
        String errorMessage = "Access denied";
        when(orderService.insert(any(OrderInsertDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andDo(print());

        verify(orderService, times(1)).
                insert(any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInInsertOrder() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInInsertOrder() throws Exception {
        String errorMessage = "Client not found";
        when(orderService.insert(any(OrderInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH).contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH))).
                andDo(print());

        verify(orderService, times(1)).
                insert(any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldDeleteOrderByUUIDAndReturn204WhenIsAuthenticated() throws Exception {
        doNothing().when(orderService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validOrderDTO.getId())).
                andExpect(status().isNoContent()).andDo(print());

        verify(orderService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{uuid}",
                        validOrderDTO.getId())).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInDeleteOrderByUUID() throws Exception {
        String errorMessage = "Order not found";
        doThrow(new EntityNotFoundException(errorMessage)).
                when(orderService).deleteByUUID(anyString());

        mvc.perform(delete(URL_PATH + "/{uuid}", validOrderDTO.getId())).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).deleteByUUID(anyString());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateOrderAsJsonByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).thenReturn(validOrderDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(1))).
                andExpect(jsonPath("$.items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldUpdateOrderAsXmlByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).thenReturn(validOrderDTO);

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).
                content(xmlMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_XML)).
                andExpect(xpath("/OrderDTO/id").string(is(validOrderDTO.getId().toString()))).
                andExpect(xpath("/OrderDTO/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/orderStatus").string(is(validOrderDTO.getOrderStatus().name()))).
                andExpect(xpath("/OrderDTO/total").number(comparesEqualTo(validOrderDTO.getTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/client/id").string(is(validOrderDTO.getClient().getId().toString()))).
                andExpect(xpath("/OrderDTO/client/name").string(is(validOrderDTO.getClient().getName()))).
                andExpect(xpath("/OrderDTO/client/email").string(is(validOrderDTO.getClient().getEmail()))).
                andExpect(xpath("/OrderDTO/client/phone").string(is(validOrderDTO.getClient().getPhone()))).
                andExpect(xpath("/OrderDTO/payment/id").string(is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/payment/order_id").string(is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(xpath("/OrderDTO/payment/moment").string(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(xpath("/OrderDTO/payment/paymentType").string(is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(xpath("/OrderDTO/payment/amount").number(comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/quantity").string(is(validOrderDTO.getItems().iterator().next().getQuantity().toString()))).
                andExpect(xpath("/OrderDTO/items/items/productPriceRecord").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/subTotal").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(xpath("/OrderDTO/items/items/product/description").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(xpath("/OrderDTO/items/items/product/price").number(comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().doubleValue()))).
                andExpect(xpath("/OrderDTO/items/items/product/imgUrl").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories").nodeCount(is(1))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/id").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(xpath("/OrderDTO/items/items/product/categories/categories/name").string(is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn400WhenSendInvalidOrderInUpdateOrderByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidOrderInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("client_id: Invalid client uuid"))).
                andExpect(jsonPath("$.errors[1]", is("orderStatus: Order status can not be null"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn402WhenIsAuthenticatedAndUpdateOrderToPaidButIsNotPaidInUpdateOrderByUUID() throws Exception {
        String errorMessage = "Not paid yet";
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).
                thenThrow(new NotPaidException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isPaymentRequired()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.PAYMENT_REQUIRED.value()))).
                andExpect(jsonPath("$.error",
                        is(NotPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn403WhenIsAuthenticatedAndUpdateOrderToWaitingPaymentButIsPaidInUpdateOrderByUUID() throws Exception {
        String errorMessage = "Already paid, unable to update order status to WAITING_PAYMENT";
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdateOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{uuid}",
                        validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndUserNotFoundInUpdateOrderByUUID() throws Exception {
        String errorMessage = "Client not found";
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInUpdateOrderByUUID() throws Exception {
        String errorMessage = "Order not found";
        when(orderService.
                updateByUUID(anyString(), any(OrderInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{uuid}", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId()))).
                andDo(print());

        verify(orderService, times(1)).
                updateByUUID(anyString(), any(OrderInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldAddItemInOrderByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.
                addItem(anyString(), any(OrderItemInsertDTO.class))).
                thenReturn(validOrderDTO);

        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(1))).
                andExpect(jsonPath("$.items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).
                addItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn400WhenSendInvalidOrderItemInAddItemInOrderByUUID() throws Exception {
        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidOrderItemInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("product_id: Invalid product uuid"))).
                andExpect(jsonPath("$.errors[1]", is("quantity: Quantity must be greater than zero"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInAddItemInOrderByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(orderService.
                addItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                addItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn403WhenIsAuthenticatedAndAddItemToPaidOrderInAddItemInOrderByUUID() throws Exception {
        String errorMessage = "Already paid, unable to do changes in this order item";
        when(orderService.
                addItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                addItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInAddItemInOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(post(URL_PATH + "/{order_uuid}/items",
                        validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInAddItemInOrderByUUID() throws Exception {
        String errorMessage = "Product not found";
        when(orderService.
                addItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                addItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInAddItemInOrderByUUID() throws Exception {
        String errorMessage = "Order not found";
        when(orderService.
                addItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(post(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                addItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldDeleteItemFromOrderByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        validOrderDTO.getItems().remove(orderItemDTO);
        when(orderService.deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenReturn(validOrderDTO);

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(0))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn400WhenSendInvalidOrderItemInDeleteItemFromOrderByUUID() throws Exception {
        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidOrderItemDeleteDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(1))).
                andExpect(jsonPath("$.errors[0]", is("product_id: Invalid product uuid"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInDeleteItemFromOrderByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(orderService.
                deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn403WhenIsAuthenticatedAndDeleteItemFromAPaidOrderInDeleteItemFromOrderByUUID() throws Exception {
        String errorMessage = "Already paid, unable to do changes in this order item";
        when(orderService.
                deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInDeleteItemFromOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(delete(URL_PATH + "/{order_uuid}/items",
                        validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInDeleteItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Product not found";
        when(orderService.
                deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInDeleteItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Order not found";
        when(orderService.
                deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderItemNotFoundInDeleteItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Order item not found";
        when(orderService.
                deleteItem(anyString(), any(OrderItemDeleteDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(delete(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemDeleteDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                deleteItem(anyString(), any(OrderItemDeleteDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldUpdateItemFromOrderByUUIDAndReturn200WhenIsAuthenticated() throws Exception {
        when(orderService.updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenReturn(validOrderDTO);

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.id", is(validOrderDTO.getId().toString()))).
                andExpect(jsonPath("$.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.orderStatus", is(validOrderDTO.getOrderStatus().name()))).
                andExpect(jsonPath("$.total", comparesEqualTo(validOrderDTO.getTotal().intValue()))).
                andExpect(jsonPath("$.client.id", is(validOrderDTO.getClient().getId().toString()))).
                andExpect(jsonPath("$.client.name", is(validOrderDTO.getClient().getName()))).
                andExpect(jsonPath("$.client.email", is(validOrderDTO.getClient().getEmail()))).
                andExpect(jsonPath("$.client.phone", is(validOrderDTO.getClient().getPhone()))).
                andExpect(jsonPath("$.payment.id", is(validOrderDTO.getPaymentDTO().getId().toString()))).
                andExpect(jsonPath("$.payment.order_id", is(validOrderDTO.getPaymentDTO().getOrder_id().toString()))).
                andExpect(jsonPath("$.payment.moment", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.payment.paymentType", is(validOrderDTO.getPaymentDTO().getPaymentType().name()))).
                andExpect(jsonPath("$.payment.amount", comparesEqualTo(validOrderDTO.getPaymentDTO().getAmount().intValue()))).
                andExpect(jsonPath("$.items.size()", is(1))).
                andExpect(jsonPath("$.items[0].quantity", is(validOrderDTO.getItems().iterator().next().getQuantity()))).
                andExpect(jsonPath("$.items[0].productPriceRecord", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductPriceRecord().intValue()))).
                andExpect(jsonPath("$.items[0].subTotal", comparesEqualTo(validOrderDTO.getItems().iterator().next().getSubTotal().intValue()))).
                andExpect(jsonPath("$.items[0].product.id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getName()))).
                andExpect(jsonPath("$.items[0].product.description", is(validOrderDTO.getItems().iterator().next().getProductDTO().getDescription()))).
                andExpect(jsonPath("$.items[0].product.price", comparesEqualTo(validOrderDTO.getItems().iterator().next().getProductDTO().getPrice().intValue()))).
                andExpect(jsonPath("$.items[0].product.imgUrl", is(validOrderDTO.getItems().iterator().next().getProductDTO().getImgUrl()))).
                andExpect(jsonPath("$.items[0].product.categories.size()", is(1))).
                andExpect(jsonPath("$.items[0].product.categories[0].id", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getId().toString()))).
                andExpect(jsonPath("$.items[0].product.categories[0].name", is(validOrderDTO.getItems().iterator().next().getProductDTO().getCategories().iterator().next().getName()))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn400WhenSendInvalidOrderItemInUpdateItemFromOrderByUUID() throws Exception {
        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidOrderItemInsertDTO))).
                andExpect(status().isBadRequest()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))).
                andExpect(jsonPath("$.errors.size()", is(2))).
                andExpect(jsonPath("$.errors[0]", is("product_id: Invalid product uuid"))).
                andExpect(jsonPath("$.errors[1]", is("quantity: Quantity must be greater than zero"))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdminAndDifferentUsersInUpdateItemFromOrderByUUID() throws Exception {
        String errorMessage = "Access denied";
        when(orderService.
                updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new AccessDeniedException(errorMessage));

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AccessDeniedException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn403WhenIsAuthenticatedAndUpdateItemFromAPaidOrderInUpdateItemFromOrderByUUID() throws Exception {
        String errorMessage = "Already paid, unable to do changes in this order item";
        when(orderService.
                updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new AlreadyPaidException(errorMessage));

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value()))).
                andExpect(jsonPath("$.error",
                        is(AlreadyPaidException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithAnonymousUser
    void shouldReturn403WhenIsNotAuthenticatedInUpdateItemFromOrderByUUID() throws Exception {
        MvcResult mvcResult = mvc.perform(put(URL_PATH + "/{order_uuid}/items",
                        validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isForbidden()).andDo(print()).andReturn();

        assertEquals("Access Denied", mvcResult.getResponse().getErrorMessage());
        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndProductNotFoundInUpdateItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Product not found";
        when(orderService.
                updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderNotFoundInUpdateItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Order not found";
        when(orderService.
                updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void shouldReturn404WhenIsAuthenticatedAndOrderItemNotFoundInUpdateItemFromOrderByUUID()  throws Exception {
        String errorMessage = "Order item not found";
        when(orderService.
                updateItem(anyString(), any(OrderItemInsertDTO.class))).
                thenThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(put(URL_PATH + "/{order_uuid}/items", validOrderDTO.getId()).
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(validOrderItemInsertDTO))).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.timestamp",
                        matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z"))).
                andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))).
                andExpect(jsonPath("$.error",
                        is(EntityNotFoundException.class.getSimpleName()))).
                andExpect(jsonPath("$.message", is(errorMessage))).
                andExpect(jsonPath("$.path", is(URL_PATH + "/" + validOrderDTO.getId() + "/items"))).
                andDo(print());

        verify(orderService, times(1)).
                updateItem(anyString(), any(OrderItemInsertDTO.class));
        verifyNoMoreInteractions(orderService);
    }

    private void init() {
        paymentDTO = new PaymentDTO(Instant.now(), PaymentType.PIX, BigDecimal.ONE);
        paymentDTO.setId(UUID.fromString(STRING_UUID));
        paymentDTO.setOrder_id(UUID.fromString(STRING_UUID));
        userDTO = new UserDTO(UUID.fromString(STRING_UUID), "Testing", "testing@gmail.com", "14784568714");
        categoryDTO = new CategoryDTO("Testing", UUID.fromString(STRING_UUID));
        productDTO = new ProductDTO(UUID.fromString(STRING_UUID), "Testing", "Testing", BigDecimal.ONE, "https://testing.com");
        productDTO.getCategories().add(categoryDTO);
        orderItemDTO = new OrderItemDTO(1, BigDecimal.ONE, BigDecimal.ONE, productDTO);
        validOrderDTO = new OrderDTO(UUID.fromString(STRING_UUID), Instant.now(), userDTO, OrderStatus.PAID, paymentDTO);
        validOrderDTO.getItems().add(orderItemDTO);
        validOrderDTO.setTotal(BigDecimal.ONE);
        validOrderInsertDTO = new OrderInsertDTO(OrderStatus.WAITING_PAYMENT, STRING_UUID);
        invalidOrderInsertDTO = new OrderInsertDTO(null, "invalid-uuid");
        validOrderItemInsertDTO = new OrderItemInsertDTO(1, STRING_UUID);
        invalidOrderItemInsertDTO = new OrderItemInsertDTO(0, "invalid-uuid");
        validOrderItemDeleteDTO = new OrderItemDeleteDTO(STRING_UUID);
        invalidOrderItemDeleteDTO = new OrderItemDeleteDTO("invalid-uuid");
    }
}