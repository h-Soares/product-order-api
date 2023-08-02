package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class ModelMapperConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        Converter<PaymentInsertDTO, Payment> paymentConverter = new AbstractConverter<>() {
            protected Payment convert(PaymentInsertDTO paymentInsertDTO) {
                Order order = orderRepository.getReferenceById(UUID.fromString(paymentInsertDTO.getOrder_id()));
                order.setOrderStatus(OrderStatus.PAID);
                return new Payment(Instant.now(), paymentInsertDTO.getPaymentType(), order);
            }
        };

        Converter<OrderInsertDTO, Order> orderConverter = new AbstractConverter<>() {
            protected Order convert(OrderInsertDTO source) {
                Order order = new Order();
                order.setMoment(Instant.now());
                order.setOrderStatus(source.getOrderStatus());
                order.setClient(userRepository.getReferenceById(UUID.fromString(source.getClient_id())));
                return order;
            }
        };

        Converter<UserInsertDTO, User> createUserConverter = new AbstractConverter<>() {
            protected User convert(UserInsertDTO source) {
                User user = new User();
                user.setName(source.getName());
                user.setEmail(source.getEmail());
                user.setPhone(source.getPhone());
                source.setPassword(bCryptPasswordEncoder.encode(source.getPassword()));
                user.setPassword(source.getPassword());
                return user;
            }
        };

        Converter<UserInsertDTO, User> updateUserConverter = context -> {
            UserInsertDTO userInsertDTO = context.getSource();
            User user = context.getDestination();

            user.setName(userInsertDTO.getName());
            user.setEmail(userInsertDTO.getEmail());
            user.setPhone(userInsertDTO.getPhone());
            userInsertDTO.setPassword(bCryptPasswordEncoder.encode(userInsertDTO.getPassword()));
            user.setPassword(userInsertDTO.getPassword());
            return user;
        };

        modelMapper.createTypeMap(UserInsertDTO.class, User.class, "createUserConverter").setConverter(createUserConverter);
        modelMapper.createTypeMap(UserInsertDTO.class, User.class, "updateUserConverter").setConverter(updateUserConverter);
        modelMapper.addConverter(paymentConverter);
        modelMapper.addConverter(orderConverter);
        return modelMapper;
    }
}