package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.dtos.OrderInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class ModelMapperConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        Converter<OrderInsertDTO, Order> orderConverter = new AbstractConverter<>() {
            protected Order convert(OrderInsertDTO source) {
                Order order = new Order();
                order.setMoment(Instant.now());
                order.setOrderStatus(source.getOrderStatus());
                order.setClient(userRepository.getReferenceById(UUID.fromString(source.getClient_id())));
                return order;
            }
        };

        modelMapper.addConverter(orderConverter);
        return modelMapper;
    }
}