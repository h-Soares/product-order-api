package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.OrderRepository;
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

    @Autowired
    private OrderRepository orderRepository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        //TESTAR SEM:
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

        modelMapper.addConverter(paymentConverter);
        modelMapper.addConverter(orderConverter);
        return modelMapper;
    }
}