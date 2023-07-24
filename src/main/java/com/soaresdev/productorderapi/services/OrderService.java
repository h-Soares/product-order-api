package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.exceptions.NotPaidException;
import com.soaresdev.productorderapi.repositories.OrderItemRepository;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.ProductRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// 20/07/2023  21:09 !
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ModelMapper modelMapper,
                        ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.modelMapper = modelMapper;
    }

    public Page<OrderDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderDTO::new);
    }

    public OrderDTO findByUUID(String uuid) {
        return new OrderDTO(getOrder(uuid));
    }

    @Transactional
    public OrderDTO insert(OrderInsertDTO orderInsertDTO) {
        if (!userRepository.existsById(UUID.fromString(orderInsertDTO.getClient_id())))
            throw new EntityNotFoundException("Client not found");
        if(orderInsertDTO.getOrderStatus() == OrderStatus.PAID)
            throw new NotPaidException("Not paid yet");

        Order order = modelMapper.map(orderInsertDTO, Order.class);
        order = orderRepository.save(order);
        System.out.println(Instant.now());
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

    @Transactional
    public OrderDTO addItem(String uuid, OrderItemInsertDTO orderItemInsertDTO) {
        if(!productRepository.existsById(UUID.fromString(orderItemInsertDTO.getProduct_id())))
            throw new EntityNotFoundException("Product not found");

        //try to make it with modelmapper...?
        Order order = getOrder(uuid);
        if(order.getPayment() != null)
            throw new AlreadyPaidException("Already paid, unable to add order item");

        if(orderItemRepository.existsById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemInsertDTO.getProduct_id()))) {
            OrderItem orderItem = orderItemRepository.findById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemInsertDTO.getProduct_id()));
            orderItem.setQuantity(orderItem.getQuantity() + orderItemInsertDTO.getQuantity());
        }
        else {
            OrderItem orderItem = new OrderItem(order, productRepository.getReferenceById(UUID.fromString(orderItemInsertDTO.getProduct_id())), orderItemInsertDTO.getQuantity());
            order.getItems().add(orderItem);
        }
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO deleteItem(String uuid, OrderItemDeleteDTO orderItemDeleteDTO) {
        if(!productRepository.existsById(UUID.fromString(orderItemDeleteDTO.getProduct_id())))
            throw new EntityNotFoundException("Product not found");

        Order order = getOrder(uuid);
        if(order.getPayment() != null)
            throw new AlreadyPaidException("Already paid, unable to delete order item");
        if(!orderItemRepository.existsById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemDeleteDTO.getProduct_id())))
            throw new EntityNotFoundException("Order item not found");

        order.getItems().remove(orderItemRepository.findById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemDeleteDTO.getProduct_id())));
        orderItemRepository.deleteById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemDeleteDTO.getProduct_id()));
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO updateItem(String uuid, OrderItemInsertDTO orderItemInsertDTO) {
        if(!productRepository.existsById(UUID.fromString(orderItemInsertDTO.getProduct_id())))
            throw new EntityNotFoundException("Product not found");

        Order order = getOrder(uuid);
        if(order.getPayment() != null)
            throw new AlreadyPaidException("Already paid, unable to update order item");
        if(!orderItemRepository.existsById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemInsertDTO.getProduct_id())))
            throw new EntityNotFoundException("Order item not found");

        OrderItem orderItem = orderItemRepository.findById_OrderIdAndId_ProductId(order.getId(), UUID.fromString(orderItemInsertDTO.getProduct_id()));
        orderItem.setQuantity(orderItemInsertDTO.getQuantity());
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    //TODO: try to make it with modelMapper
    private void updateOrder(Order order, OrderInsertDTO orderInsertDTO) {
        if (!userRepository.existsById(UUID.fromString(orderInsertDTO.getClient_id())))
            throw new EntityNotFoundException("Client not found");
        if(orderInsertDTO.getOrderStatus() == OrderStatus.PAID && order.getPayment() == null)
            throw new NotPaidException("Not paid yet");
        if(order.getPayment() != null && orderInsertDTO.getOrderStatus() == OrderStatus.WAITING_PAYMENT)
            throw new AlreadyPaidException("Already paid, unable to update order status to WAITING_PAYMENT");

        order.setOrderStatus(orderInsertDTO.getOrderStatus());
        order.setClient(userRepository.getReferenceById(UUID.fromString(orderInsertDTO.getClient_id())));
    }

    private Order getOrder(String uuid) {
        return orderRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    //TODO: remove comments, README, DEPLOY (done).
}