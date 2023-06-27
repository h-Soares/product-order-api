package com.soaresdev.productorderapi;

import com.soaresdev.productorderapi.entities.*;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.*;
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

    final
    OrderItemRepository orderItemRepository;

    public ProductOrderApiApplication(UserRepository userRepository, OrderRepository orderRepository,
                                      CategoryRepository categoryRepository, ProductRepository productRepository,
                                      OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductOrderApiApplication.class, args);
    }

    @Override //to test
    public void run(String... args) throws Exception {
        User u0 = new User("Test", "testing@gmail.com", "teste123phone", "test123");
        User u1 = new User("Maria Brown", "maria@gmail.com", "988888888", "123456");
        User u2 = new User("Alex Green", "alex@gmail.com", "977777777", "123456");
        userRepository.saveAll(List.of(u0, u1, u2));

        Order o0 = new Order(Instant.now(), OrderStatus.SHIPPED, u0);
        Order o1 = new Order(Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.CANCELED,u1);
        Order o2 = new Order(Instant.parse("2019-07-21T03:42:10Z"), OrderStatus.DELIVERED,u2);
        Order o3 = new Order(Instant.parse("2019-07-22T15:21:22Z"), OrderStatus.WAITING_PAYMENT,u1);
        Order o4 = new Order(Instant.parse("2019-07-22T15:21:22Z"), OrderStatus.DELIVERED,u1);
        orderRepository.saveAll(List.of(o0, o1, o2, o3,o4));

        Category cat1 = new Category("Electronics");
        Category cat2 = new Category("Books");
        Category cat3 = new Category("Computers");
        categoryRepository.saveAll(List.of(cat1, cat2, cat3));

        Product p0 = new Product("Testing", "", BigDecimal.ONE, "");
        Product p1 = new Product("The Lord of the Rings", "Lorem ipsum dolor sit amet, consectetur.", BigDecimal.valueOf(90.5), "");
        Product p2 = new Product("Smart TV", "Nulla eu imperdiet purus. Maecenas ante.", BigDecimal.valueOf(2190.0), "");
        Product p3 = new Product("Macbook Pro", "Nam eleifend maximus tortor, at mollis.", BigDecimal.valueOf(1250.0), "");
        Product p4 = new Product("PC Gamer", "Donec aliquet odio ac rhoncus cursus.", BigDecimal.valueOf(1200.0), "");
        Product p5 = new Product("Rails for Dummies", "Cras fringilla convallis sem vel faucibus.", BigDecimal.valueOf(100.99), "");
        p0.getCategories().addAll(Set.of(cat1, cat3));
        p1.getCategories().add(cat2);
        p2.getCategories().add(cat1);
        p3.getCategories().addAll(Set.of(cat1,cat3));
        p4.getCategories().addAll(Set.of(cat1,cat3));
        p5.getCategories().add(cat2);
        productRepository.saveAll(List.of(p0,p1,p2,p3,p4,p5));


        p1.setPrice(BigDecimal.TEN);
        productRepository.save(p1);

        o0.getItems().add(new OrderItem(o0, p1, 5));
        o0.getItems().add(new OrderItem(o0, p3, 14));
        orderRepository.save(o0);

        OrderItem oi1 = new OrderItem(o3,p4, 6);
        OrderItem oi2 = new OrderItem(o3,p5, 1);
        o3.getItems().add(oi1);
        o3.getItems().add(oi2);
        orderRepository.save(o3);

        //userRepository.delete(u1);

//        o3.getItems().remove(oi1);
//        orderItemRepository.delete(oi1);
//        orderItemRepository.deleteByOrderAndProduct(o3,p4);

        //userRepository.delete(u1);
        //productRepository.delete(p4);
    }
}