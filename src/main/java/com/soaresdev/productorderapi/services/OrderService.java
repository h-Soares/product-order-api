package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAll().stream().map(OrderDTO::new).toList(); /* TODO: page */
    }

    public OrderDTO findByUUID(String uuid) {
        Optional<Order> optionalOrder = orderRepository.findById(UUID.fromString(uuid));
        return new OrderDTO(optionalOrder.get()); /* TODO: handle exception */
    }

                           //VER AS ABAS ABERTAS!
    /* TODO: PEGAR A LISTA DE ITENS E ADICIONAR UM ITEM (PRODUTO),
             CRIAR SERVICE PARA OrderItem,
             VERIFICAR SE JÁ EXISTE COM OrderItemRepository boolean existsByOrderAndProduct
    */
}