package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DataJpaTest
@ActiveProfiles(value = "test")
class PaymentRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setup() {
        user = new User("t", "t", "t", "t");
        user = userRepository.save(user);

        order = new Order(Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        order = orderRepository.save(order);

        payment = new Payment(Instant.now(), PaymentType.PIX, order);
        payment = paymentRepository.save(payment);
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    void shouldExistsByOrderId() {
        assertTrue(paymentRepository.existsByOrderId(order.getId()));
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    void shouldNotExistsByOrderId() {
        assertFalse(paymentRepository.existsByOrderId(UUID.randomUUID()));
    }

    @Test
    void shouldDeleteByUUID() {
        paymentRepository.deleteByUUID(payment.getId());
        assertFalse(paymentRepository.existsById(payment.getId()));
    }

    @Test
    void shouldFindAllWithPage() {
        Order order1 = new Order(Instant.now(), OrderStatus.DELIVERED, user);
        order1 = orderRepository.save(order1);

        Payment payment2 = new Payment(Instant.now(), PaymentType.CREDIT_CARD, order1);
        payment2 = paymentRepository.save(payment2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> result = paymentRepository.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(payment, result.getContent().get(0));
        assertEquals(payment2, result.getContent().get(1));
    }
}