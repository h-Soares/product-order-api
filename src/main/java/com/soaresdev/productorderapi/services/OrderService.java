package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.OrderInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAll().stream().map(OrderDTO::new).toList(); /* TODO: page */
    }

    public OrderDTO findByUUID(String uuid) {
        return new OrderDTO(getOrder(uuid));
    }

    @Transactional
    public OrderDTO insert(OrderInsertDTO orderInsertDTO) {
        if (!userRepository.existsById(UUID.fromString(orderInsertDTO.getClient_id())))
            throw new EntityNotFoundException("Client not found");

        Order order = modelMapper.map(orderInsertDTO, Order.class);
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        orderRepository.delete(getOrder(uuid));
    }

    @Transactional
    public OrderDTO updateByUUID(String uuid, OrderInsertDTO orderInsertDTO) {
        Order order = getOrder(uuid);
        updateOrder(order, orderInsertDTO);
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    private void updateOrder(Order order, OrderInsertDTO orderInsertDTO) {
        if (!userRepository.existsById(UUID.fromString(orderInsertDTO.getClient_id())))
            throw new EntityNotFoundException("Client not found");
        if(orderInsertDTO.getOrderStatus() == OrderStatus.PAID && order.getPayment() == null)
            throw new IllegalArgumentException("Not paid yet");

        order.setOrderStatus(orderInsertDTO.getOrderStatus());
        order.setClient(userRepository.getReferenceById(UUID.fromString(orderInsertDTO.getClient_id())));
    }

    private Order getOrder(String uuid) {
        return orderRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    /* TODO: PEGAR A LISTA DE ITENS E ADICIONAR UM ITEM (PRODUTO),
             CRIAR SERVICE PARA OrderItem (??), USAR OrderItemRepository
             VERIFICAR SE JÁ EXISTE COM OrderItemRepository boolean existsByOrderAndProduct
             CRIAR PaymentService (set orderstatus -> Paid em cascata, quando inserir um novo) , PaymentRepository
    */
}