package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.entities.*;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.exceptions.NotPaidException;
import com.soaresdev.productorderapi.repositories.OrderItemRepository;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.ProductRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ModelMapper modelMapper;

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    private User client;
    private User differentClient;
    private Order order;
    private Product product;
    private OrderInsertDTO orderInsertDTO;
    private Payment payment;
    private OrderItem orderItem;
    private OrderItemInsertDTO orderItemInsertDTO;
    private OrderItemDeleteDTO orderItemDeleteDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldFindAllOrders() {
        when(orderRepository.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderDTO> result = orderService.findAll(PageRequest.of(0, 2));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getMoment(), result.getContent().get(0).getMoment());
        assertEquals(order.getOrderStatus(), result.getContent().get(0).getOrderStatus().getCode());
        assertEquals(order.getTotal(), result.getContent().get(0).getTotal());
        assertNull(order.getPayment());
        assertNull(result.getContent().get(0).getPaymentDTO());
        assertEquals(order.getClient().getEmail(), result.getContent().get(0).getClient().getEmail());
        assertTrue(order.getItems().isEmpty());
        assertTrue(result.getContent().get(0).getItems().isEmpty());
        verify(orderRepository,times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldFindOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);

        OrderDTO responseOrder = orderService.findByUUID(RANDOM_UUID.toString());

        assertNotNull(responseOrder);
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertTrue(order.getItems().isEmpty());
        assertTrue(responseOrder.getItems().isEmpty());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInFindOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Order not found", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotManagerOrAdminAndDifferentUsersInFindOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(differentClient);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> orderService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Access denied", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldInsertOrder() {
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        mockGetPrincipalReturns(client);
        when(userRepository.getReferenceById(any(UUID.class))).thenReturn(client);
        when(modelMapper.map(any(OrderInsertDTO.class), eq(Order.class))).
                thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.insert(orderInsertDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertTrue(order.getItems().isEmpty());
        assertTrue(responseOrder.getItems().isEmpty());
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verify(userRepository, times(1)).getReferenceById(any(UUID.class));
        verify(modelMapper, times(1)).
                map(any(OrderInsertDTO.class), eq(Order.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(modelMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenClientInOrderNotExistsInInsertOrder() {
        when(userRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.insert(orderInsertDTO));
        assertEquals("Client not found", e.getMessage());
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInInsertOrder() {
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        mockGetPrincipalReturns(client);
        when(userRepository.getReferenceById(any(UUID.class))).thenReturn(differentClient);


        Throwable e = assertThrows(AccessDeniedException.class,
                () -> orderService.insert(orderInsertDTO));
        assertEquals("Access denied", e.getMessage());
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verify(userRepository, times(1)).getReferenceById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowNotPaidExceptionWhenTryToInsertAPaidOrderInInsertOrder() {
        orderInsertDTO.setOrderStatus(OrderStatus.PAID);
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        mockGetPrincipalReturns(client);
        when(userRepository.getReferenceById(any(UUID.class))).thenReturn(client);

        Throwable e = assertThrows(NotPaidException.class,
                () -> orderService.insert(orderInsertDTO));
        assertEquals("Not paid yet", e.getMessage());
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verify(userRepository, times(1)).getReferenceById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldDeleteOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        doNothing().when(orderRepository).delete(any(Order.class));

        orderService.deleteByUUID(RANDOM_UUID.toString());

        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(orderRepository, times(1)).delete(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInDeleteOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("Order not found", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldUpdateOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        when(userRepository.getReferenceById(any(UUID.class))).thenReturn(differentClient);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.updateByUUID(RANDOM_UUID.toString(), orderInsertDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertTrue(order.getItems().isEmpty());
        assertTrue(responseOrder.getItems().isEmpty());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verify(userRepository, times(1)).getReferenceById(any(UUID.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInUpdateOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateByUUID(RANDOM_UUID.toString(), orderInsertDTO));
        assertEquals("Order not found", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenClientInOrderNotExistsInUpdateOrderByUUID() {
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        when(userRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateByUUID(RANDOM_UUID.toString(), orderInsertDTO));
        assertEquals("Client not found", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowNotPaidExceptionWhenOrderStatusToPaidAndPaymentIsNullInUpdateOrderByUUID() {
        orderInsertDTO.setOrderStatus(OrderStatus.PAID);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);

        Throwable e = assertThrows(NotPaidException.class,
                () -> orderService.updateByUUID(RANDOM_UUID.toString(), orderInsertDTO));
        assertEquals("Not paid yet", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenPaymentIsNotNullAndOrderStatusToWaitingPaymentInUpdateOrderByUUID() {
        order.setPayment(payment);
        order.setOrderStatus(OrderStatus.PAID);
        orderInsertDTO.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> orderService.updateByUUID(RANDOM_UUID.toString(), orderInsertDTO));
        assertEquals("Already paid, unable to update order status to WAITING_PAYMENT", e.getMessage());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldAddItemInOrderWhenItemNotExistsInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class))).
                thenReturn(false);
        when(productRepository.getReferenceById(any(UUID.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertFalse(order.getItems().isEmpty());
        assertFalse(responseOrder.getItems().isEmpty());
        assertEquals(1, responseOrder.getItems().size());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertEquals(order.getItems().iterator().next().getQuantity(),
                responseOrder.getItems().iterator().next().getQuantity());
        assertEquals(order.getItems().iterator().next().getProductPriceRecord(),
                responseOrder.getItems().iterator().next().getProductPriceRecord());
        assertEquals(order.getItems().iterator().next().getSubTotal(),
                responseOrder.getItems().iterator().next().getSubTotal());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class));
        verify(productRepository, times(1)).getReferenceById(any(UUID.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderItemRepository);
    }

    @Test
    void shouldAddItemInOrderWhenItemExistsInOrder() {
        order.getItems().add(orderItem);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class))).
                thenReturn(true);
        when(orderItemRepository.
                findById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class))).
                thenReturn(orderItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertFalse(order.getItems().isEmpty());
        assertFalse(responseOrder.getItems().isEmpty());
        assertEquals(1, responseOrder.getItems().size());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertEquals(order.getItems().iterator().next().getQuantity(),
                responseOrder.getItems().iterator().next().getQuantity());
        assertEquals(order.getItems().iterator().next().getProductPriceRecord(),
                responseOrder.getItems().iterator().next().getProductPriceRecord());
        assertEquals(order.getItems().iterator().next().getSubTotal(),
                responseOrder.getItems().iterator().next().getSubTotal());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class));
        verify(orderItemRepository, times(1)).
                findById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInAddItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInAddItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Order not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInAddItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(differentClient);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Access denied", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenOrderIsPaidInAddItemInOrder() {
        order.setPayment(payment);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> orderService.addItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Already paid, unable to do changes in this order item", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldDeleteItemInOrder() {
        order.getItems().add(orderItem);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(true);
        when(orderItemRepository.
                findById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(orderItem);
        doNothing().when(orderItemRepository).
                deleteById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertTrue(order.getItems().isEmpty());
        assertTrue(responseOrder.getItems().isEmpty());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        verify(orderItemRepository, times(1)).
                findById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        verify(orderItemRepository, times(1)).
                deleteById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(orderItemRepository);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInDeleteItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInDeleteItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO));
        assertEquals("Order not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInDeleteItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(differentClient);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO));
        assertEquals("Access denied", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenOrderIsPaidInDeleteItemInOrder() {
        order.setPayment(payment);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO));
        assertEquals("Already paid, unable to do changes in this order item", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderItemNotExistsInDeleteItemInOrder() {
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteItem(RANDOM_UUID.toString(), orderItemDeleteDTO));
        assertEquals("Order item not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderItemRepository);
    }

    @Test
    void shouldUpdateItemInOrder() {
        order.getItems().add(orderItem);
        orderItemInsertDTO.setQuantity(13);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(true);
        when(orderItemRepository.
                findById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(orderItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> {
            order.setId(RANDOM_UUID);
            return order;
        });

        OrderDTO responseOrder = orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO);

        assertNotNull(responseOrder);
        assertEquals(order.getId(), responseOrder.getId());
        assertEquals(order.getMoment(), responseOrder.getMoment());
        assertEquals(order.getOrderStatus(), responseOrder.getOrderStatus().getCode());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertNull(order.getPayment());
        assertNull(responseOrder.getPaymentDTO());
        assertEquals(order.getClient().getEmail(), responseOrder.getClient().getEmail());
        assertFalse(order.getItems().isEmpty());
        assertFalse(responseOrder.getItems().isEmpty());
        assertEquals(1, responseOrder.getItems().size());
        assertEquals(order.getTotal(), responseOrder.getTotal());
        assertEquals(order.getItems().iterator().next().getQuantity(),
                responseOrder.getItems().iterator().next().getQuantity());
        assertEquals(order.getItems().iterator().next().getProductPriceRecord(),
                responseOrder.getItems().iterator().next().getProductPriceRecord());
        assertEquals(order.getItems().iterator().next().getSubTotal(),
                responseOrder.getItems().iterator().next().getSubTotal());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class));
        verify(orderItemRepository, times(1)).
                findById_OrderIdAndId_ProductId(any(UUID.class), any(UUID.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInUpdateItemInOrder() {
        orderItemInsertDTO.setQuantity(13);
        when(productRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderNotExistsInUpdateItemInOrder() {
        orderItemInsertDTO.setQuantity(13);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Order not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInUpdateItemInOrder() {
        orderItemInsertDTO.setQuantity(13);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(differentClient);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Access denied", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenOrderIsPaidInUpdateItemInOrder() {
        orderItemInsertDTO.setQuantity(13);
        order.setPayment(payment);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Already paid, unable to do changes in this order item", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenOrderItemNotExistsInUpdateItemInOrder() {
        orderItemInsertDTO.setQuantity(13);
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);
        when(orderRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(order));
        mockGetPrincipalReturns(client);
        when(orderItemRepository.
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID)).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateItem(RANDOM_UUID.toString(), orderItemInsertDTO));
        assertEquals("Order item not found", e.getMessage());
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(orderRepository,times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).
                existsById_OrderIdAndId_ProductId(RANDOM_UUID, RANDOM_UUID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(orderItemRepository);
    }


    private void init() {
        Role role = new Role(RoleName.ROLE_USER.getCode());
        client = new User("test", "test@gmail.com", "test", "test");
        client.getRoles().add(role);
        differentClient = new User("Test2", "test2@gmail.com", "test2", "test2");
        differentClient.getRoles().add(role);
        order = new Order(Instant.now(), OrderStatus.DELIVERED, client);
        order.setId(RANDOM_UUID);
        orderInsertDTO = new OrderInsertDTO(OrderStatus.CANCELED, RANDOM_UUID.toString());
        payment = new Payment(Instant.now(), PaymentType.PIX, order);
        product = new Product("Test", "Test", BigDecimal.ONE, "Test");
        orderItem = new OrderItem(order, product, 3);
        orderItemInsertDTO = new OrderItemInsertDTO(2, RANDOM_UUID.toString());
        orderItemDeleteDTO = new OrderItemDeleteDTO(RANDOM_UUID.toString());
    }

    private void mockGetPrincipalReturns(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(auth.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }
}