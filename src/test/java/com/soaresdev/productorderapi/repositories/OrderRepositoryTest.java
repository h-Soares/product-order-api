package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles(value = "test")
class OrderRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldFindAllOrdersWithPage() {
        User user = new User("t", "t", "t", "t");
        user = userRepository.save(user);
        Order order1 = new Order(Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        Order order2 = new Order(Instant.now(), OrderStatus.DELIVERED, user);
        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> result = orderRepository.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(order1, result.getContent().get(0));
        assertEquals(order2, result.getContent().get(1));
    }
}