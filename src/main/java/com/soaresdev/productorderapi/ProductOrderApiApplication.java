package com.soaresdev.productorderapi;

import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class ProductOrderApiApplication implements CommandLineRunner {
    final
    UserRepository userRepository;

    final
    OrderRepository orderRepository;

    final
    CategoryRepository categoryRepository;

    public ProductOrderApiApplication(UserRepository userRepository, OrderRepository orderRepository,
                                      CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
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
    }
}