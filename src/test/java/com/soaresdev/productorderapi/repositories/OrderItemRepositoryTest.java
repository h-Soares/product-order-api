package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.pk.OrderItemPK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DataJpaTest
@ActiveProfiles(value = "test")
class OrderItemRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private Order order;
    private Product product;

    @BeforeEach
    void setup() {
        User user = new User("t", "t", "t", "t");
        user = userRepository.save(user);
        order = new Order(Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        order = orderRepository.save(order);

        product = new Product("Test", "Test", BigDecimal.ONE, "Test");
        product = productRepository.save(product);

        orderItemRepository.save(new OrderItem(order, product, 1));
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    void shouldOrderItemExistsByOrderIdAndProductId() {
        assertTrue(orderItemRepository.existsById_OrderIdAndId_ProductId(order.getId(),product.getId()));
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    void shouldOrderItemNotExistsByOrderIdAndProductId() {
        assertFalse(orderItemRepository.existsById_OrderIdAndId_ProductId(UUID.randomUUID(), product.getId()));
    }

    @Test
    void shouldDeleteOrderItemByOrderIdAndProductId() {
        orderItemRepository.deleteById_OrderIdAndId_ProductId(order.getId(), product.getId());

        OrderItemPK orderItemPK = new OrderItemPK();
        orderItemPK.setOrder(order);
        orderItemPK.setProduct(product);

        assertFalse(orderItemRepository.existsById(orderItemPK));
    }

    @org.junit.jupiter.api.Order(3)
    @Test
    void shouldFindOrderItemByOrderIdAndProductId() {
        OrderItem orderItem = orderItemRepository.findById_OrderIdAndId_ProductId(order.getId(), product.getId());

        assertNotNull(orderItem);
        assertEquals(order, orderItem.getOrder());
        assertEquals(product, orderItem.getProduct());
    }

    @org.junit.jupiter.api.Order(4)
    @Test
    void shouldNotFindOrderItemByOrderIdAndProductId() {
        OrderItem orderItem = orderItemRepository.findById_OrderIdAndId_ProductId(UUID.randomUUID(), product.getId());

        assertNull(orderItem);
    }
}