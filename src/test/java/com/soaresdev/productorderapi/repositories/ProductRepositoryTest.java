package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles(value = "test")
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldFindAllProductsWithPage() {
        Product product1 = new Product("t", "t", BigDecimal.ONE, "t");
        product1 = productRepository.save(product1);
        Product product2 = new Product("T", "T", BigDecimal.ONE, "T");
        product2 = productRepository.save(product2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(product1, result.getContent().get(0));
        assertEquals(product2, result.getContent().get(1));
    }
}