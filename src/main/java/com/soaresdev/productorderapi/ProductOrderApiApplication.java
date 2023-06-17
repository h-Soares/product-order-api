package com.soaresdev.productorderapi;

import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.ProductRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class ProductOrderApiApplication implements CommandLineRunner {
    final
    UserRepository userRepository;

    final
    OrderRepository orderRepository;

    final
    CategoryRepository categoryRepository;

    final
    ProductRepository productRepository;

    public ProductOrderApiApplication(UserRepository userRepository, OrderRepository orderRepository,
                                      CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductOrderApiApplication.class, args);
    }

    @Override //to test
    public void run(String... args) throws Exception {
        User user = new User("Test", "testing@gmail.com", "teste123phone", "test123");
        Order order = new Order(Instant.now(), OrderStatus.SHIPPED, user);
        userRepository.save(user);
        orderRepository.save(order);

        Category cat1 = new Category("Electronics");
        Category cat2 = new Category("Books");
        Category cat3 = new Category("Computers");
        categoryRepository.saveAll(List.of(cat1, cat2, cat3));

        Product product = new Product("Testing", "", BigDecimal.ONE, "");
        product.getCategories().addAll(Set.of(cat1, cat2));
        productRepository.save(product);
    }
}