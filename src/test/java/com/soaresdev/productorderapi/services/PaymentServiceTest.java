package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.PaymentDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.PaymentRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    private Payment payment;
    private PaymentInsertDTO paymentInsertDTO;
    private Order order;
    private User client;
    private User differentClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldFindAllPayments() {
        when(paymentRepository.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(payment)));

        Page<PaymentDTO> result = paymentService.findAll(PageRequest.of(0, 2));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(payment.getMoment(), result.getContent().get(0).getMoment());
        assertEquals(payment.getPaymentType(), result.getContent().get(0).getPaymentType().getCode());
        assertEquals(payment.getAmount(), result.getContent().get(0).getAmount());
        assertEquals(payment.getOrder().getId(), result.getContent().get(0).getOrder_id());
        verify(paymentRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldFindPaymentByUUID() {
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        mockGetPrincipalReturns(client);

        PaymentDTO responsePayment = paymentService.findByUUID(RANDOM_UUID.toString());

        assertNotNull(responsePayment);
        assertEquals(payment.getMoment(), responsePayment.getMoment());
        assertEquals(payment.getPaymentType(), responsePayment.getPaymentType().getCode());
        assertEquals(payment.getAmount(), responsePayment.getAmount());
        assertEquals(payment.getOrder().getId(), responsePayment.getOrder_id());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentNotExistsInFindPaymentByUUID() {
        when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> paymentService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Payment not found", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotManagerOrAdminAndDifferentUsersInFindPaymentByUUID() {
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        mockGetPrincipalReturns(differentClient);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> paymentService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Access denied", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldInsertPayment() {
        when(orderRepository.existsById(any(UUID.class))).thenReturn(true);
        when(paymentRepository.existsByOrderId(any(UUID.class))).thenReturn(false);
        mockGetPrincipalReturns(client);
        when(orderRepository.getReferenceById(any(UUID.class))).thenReturn(order);
        when(modelMapper.map(any(PaymentInsertDTO.class), eq(Payment.class))).
                thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> {
            payment.setId(RANDOM_UUID);
            return payment;
        });

        PaymentDTO responsePayment = paymentService.insert(paymentInsertDTO);

        assertNotNull(responsePayment);
        assertEquals(payment.getId(), responsePayment.getId());
        assertEquals(payment.getMoment(), responsePayment.getMoment());
        assertEquals(payment.getPaymentType(), responsePayment.getPaymentType().getCode());
        assertEquals(payment.getAmount(), responsePayment.getAmount());
        assertEquals(payment.getOrder().getId(), responsePayment.getOrder_id());
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verify(paymentRepository, times(1)).existsByOrderId(any(UUID.class));
        verify(orderRepository, times(1)).getReferenceById(any(UUID.class));
        verify(modelMapper, times(1)).
                map(any(PaymentInsertDTO.class), eq(Payment.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(modelMapper);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentOrderNotExistsInInsertPayment() {
        when(orderRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> paymentService.insert(paymentInsertDTO));
        assertEquals("Order not found", e.getMessage());
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenExistsPaymentByOrderIdInInsertPayment() {
        when(orderRepository.existsById(any(UUID.class))).thenReturn(true);
        when(paymentRepository.existsByOrderId(any(UUID.class))).thenReturn(true);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> paymentService.insert(paymentInsertDTO));
        assertEquals("Order already paid", e.getMessage());
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verify(paymentRepository, times(1)).existsByOrderId(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotManagerOrAdminAndDifferentUsersInInsertPayment() {
        when(orderRepository.existsById(any(UUID.class))).thenReturn(true);
        when(paymentRepository.existsByOrderId(any(UUID.class))).thenReturn(false);
        mockGetPrincipalReturns(differentClient);
        when(orderRepository.getReferenceById(any(UUID.class))).thenReturn(order);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> paymentService.insert(paymentInsertDTO));
        assertEquals("Access denied", e.getMessage());
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verify(paymentRepository, times(1)).existsByOrderId(any(UUID.class));
        verify(orderRepository, times(1)).getReferenceById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldDeletePaymentByUUID() {
        payment.setId(RANDOM_UUID);
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        doNothing().when(paymentRepository).deleteByUUID(any(UUID.class));

        paymentService.deleteByUUID(RANDOM_UUID.toString());

        assertEquals(OrderStatus.WAITING_PAYMENT.getCode(), payment.getOrder().getOrderStatus());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verify(paymentRepository, times(1)).deleteByUUID(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentNotExistsInDeletePaymentByUUID() {
        when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> paymentService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("Payment not found", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldUpdatePaymentByUUID() {
        Order newOrder = getNewOrder();
        paymentInsertDTO.setPaymentType(PaymentType.CREDIT_CARD);
        paymentInsertDTO.setOrder_id(newOrder.getId().toString());
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        when(orderRepository.existsById(any(UUID.class))).thenReturn(true);
        when(paymentRepository.existsByOrderId(any(UUID.class))).thenReturn(false);
        when(orderRepository.getReferenceById(any(UUID.class))).thenReturn(newOrder);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> {
            payment.setId(RANDOM_UUID);
            return payment;
        });

        PaymentDTO responsePayment = paymentService.updateByUUID(RANDOM_UUID.toString(), paymentInsertDTO);

        assertNotNull(responsePayment);
        assertEquals(payment.getId(), responsePayment.getId());
        assertEquals(payment.getMoment(), responsePayment.getMoment());
        assertEquals(payment.getPaymentType(), responsePayment.getPaymentType().getCode());
        assertEquals(payment.getAmount(), responsePayment.getAmount());
        assertEquals(payment.getOrder().getId(), responsePayment.getOrder_id());
        assertEquals(OrderStatus.PAID.getCode(), payment.getOrder().getOrderStatus());
        assertEquals(OrderStatus.WAITING_PAYMENT.getCode(), order.getOrderStatus());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verify(paymentRepository, times(1)).existsByOrderId(any(UUID.class));
        verify(orderRepository, times(1)).getReferenceById(any(UUID.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentNotExistsInUpdatePaymentByUUID() {
        Order newOrder = getNewOrder();
        paymentInsertDTO.setPaymentType(PaymentType.CREDIT_CARD);
        paymentInsertDTO.setOrder_id(newOrder.getId().toString());
        when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> paymentService.updateByUUID(RANDOM_UUID.toString(), paymentInsertDTO));
        assertEquals("Payment not found", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPaymentOrderNotExistsInUpdatePaymentByUUID() {
        Order newOrder = getNewOrder();
        paymentInsertDTO.setPaymentType(PaymentType.CREDIT_CARD);
        paymentInsertDTO.setOrder_id(newOrder.getId().toString());
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        when(orderRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> paymentService.updateByUUID(RANDOM_UUID.toString(), paymentInsertDTO));
        assertEquals("Order not found", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void shouldThrowAlreadyPaidExceptionWhenPaymentOrderIdIsDifferentAndIsPaidInUpdatePaymentByUUID() {
        Order newOrder = getNewOrder();
        paymentInsertDTO.setPaymentType(PaymentType.CREDIT_CARD);
        paymentInsertDTO.setOrder_id(newOrder.getId().toString());
        when(paymentRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(payment));
        when(orderRepository.existsById(any(UUID.class))).thenReturn(true);
        when(paymentRepository.existsByOrderId(any(UUID.class))).thenReturn(true);

        Throwable e = assertThrows(AlreadyPaidException.class,
                () -> paymentService.updateByUUID(RANDOM_UUID.toString(), paymentInsertDTO));
        assertEquals("Order already paid", e.getMessage());
        verify(paymentRepository, times(1)).findById(any(UUID.class));
        verify(orderRepository, times(1)).existsById(any(UUID.class));
        verify(paymentRepository, times(1)).existsByOrderId(any(UUID.class));
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(orderRepository);
    }

    private void init() {
        Role role = new Role(RoleName.ROLE_USER.getCode());
        client = new User("test", "test@gmail.com", "test", "test");
        client.getRoles().add(role);
        differentClient = new User("Test2", "test2@gmail.com", "test2", "test2");
        differentClient.getRoles().add(role);
        order = new Order(Instant.now(), OrderStatus.DELIVERED, client);
        order.setId(RANDOM_UUID);
        payment = new Payment(Instant.now(), PaymentType.PIX, order);
        paymentInsertDTO = new PaymentInsertDTO(PaymentType.PIX, RANDOM_UUID.toString());
    }

    private Order getNewOrder() {
        UUID newOrderUuid = UUID.randomUUID();
        Order newOrder = new Order(Instant.now(), OrderStatus.CANCELED, client);
        newOrder.setId(newOrderUuid);
        return newOrder;
    }

    private void mockGetPrincipalReturns(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(auth.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }
}