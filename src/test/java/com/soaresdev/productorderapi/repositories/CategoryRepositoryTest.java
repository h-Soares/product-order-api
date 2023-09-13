package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.entities.Product;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DataJpaTest
@ActiveProfiles(value = "test")
class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository; //to test in tb_product_category

    @Order(1)
    @Test
    void shouldExistsByNameBeTrue() {
        categoryRepository.save(new Category("Test"));

        assertTrue(categoryRepository.existsByName("Test"));
    }

    @Order(2)
    @Test
    void shouldExistsByNameBeFalse() {
        categoryRepository.save(new Category("TestTwo"));

        assertFalse(categoryRepository.existsByName("Test"));
    }

    @Order(3)
    @Test
    void shouldDeleteByUUID() {
        Category category = new Category("TestDelete");
        category = categoryRepository.save(category);
        UUID uuid = category.getId();

        Product product = new Product("t", "t", BigDecimal.ZERO,"t");
        product.getCategories().add(category);
        product = productRepository.save(product);

        categoryRepository.deleteByUUID(uuid);

        boolean isPresent = categoryRepository.findById(uuid).isPresent();
        assertFalse(isPresent);
    }

    @Order(4)
    @Test
    void shouldFindAllWithPage() {
        Category category1 = new Category("Category 1");
        Category category2 = new Category("Category 2");

        category1 = categoryRepository.save(category1);
        category2 = categoryRepository.save(category2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = categoryRepository.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(category1, result.getContent().get(0));
        assertEquals(category2, result.getContent().get(1));
    }
}