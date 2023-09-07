package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.OrderDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemDeleteDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.OrderItemInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.RoleName;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static com.soaresdev.productorderapi.utils.Utils.getContextUser;

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
        Order order = getOrder(uuid);
        User contextUser = getContextUser();
        if(contextUser.getRoleNames().stream().noneMatch(r -> r.equals(RoleName.ROLE_MANAGER.toString()) ||
                r.equals(RoleName.ROLE_ADMIN.toString())))
            ifUserIsNotSameThrowsException(order.getClient(), contextUser);

        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO insert(OrderInsertDTO orderInsertDTO) {
        ifClientNotExistsThrowsException(orderInsertDTO.getClient_id());
        if(!isContextUserAdmin()) {
            User client = userRepository.getReferenceById(UUID.fromString(orderInsertDTO.getClient_id()));
            ifUserIsNotSameThrowsException(client, getContextUser());
        }
        if(orderInsertDTO.getOrderStatus() == OrderStatus.PAID)
            throw new NotPaidException("Not paid yet");

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

    @Transactional
    public OrderDTO addItem(String uuid, OrderItemInsertDTO orderItemInsertDTO) {
        ifProductNotExistsThrowsException(orderItemInsertDTO.getProduct_id());

        Order order = getOrder(uuid);
        if(!isContextUserAdmin())
            ifUserIsNotSameThrowsException(order.getClient(), getContextUser());
        ifOrderIsAlreadyPaidThrowsException(order);

        UUID productUuid = UUID.fromString(orderItemInsertDTO.getProduct_id());
        if(orderItemRepository.existsById_OrderIdAndId_ProductId(order.getId(), productUuid)) {
            OrderItem orderItem = getOrderItem(order.getId(), productUuid);
            orderItem.setQuantity(orderItem.getQuantity() + orderItemInsertDTO.getQuantity());
        }
        else {
            OrderItem orderItem = new OrderItem(order, productRepository.getReferenceById(productUuid), orderItemInsertDTO.getQuantity());
            order.getItems().add(orderItem);
        }
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO deleteItem(String uuid, OrderItemDeleteDTO orderItemDeleteDTO) {
        ifProductNotExistsThrowsException(orderItemDeleteDTO.getProduct_id());

        Order order = getOrder(uuid);
        if(!isContextUserAdmin())
            ifUserIsNotSameThrowsException(order.getClient(), getContextUser());
        ifOrderIsAlreadyPaidThrowsException(order);
        UUID productUuid = UUID.fromString(orderItemDeleteDTO.getProduct_id());
        ifOrderItemNotExistsThrowsException(order.getId(), productUuid);

        order.getItems().remove(getOrderItem(order.getId(), productUuid));
        orderItemRepository.deleteById_OrderIdAndId_ProductId(order.getId(), productUuid);
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO updateItem(String uuid, OrderItemInsertDTO orderItemInsertDTO) {
        ifProductNotExistsThrowsException(orderItemInsertDTO.getProduct_id());

        Order order = getOrder(uuid);
        if(!isContextUserAdmin())
            ifUserIsNotSameThrowsException(order.getClient(), getContextUser());
        ifOrderIsAlreadyPaidThrowsException(order);

        UUID productUuid = UUID.fromString(orderItemInsertDTO.getProduct_id());
        ifOrderItemNotExistsThrowsException(order.getId(), productUuid);

        OrderItem orderItem = getOrderItem(order.getId(), productUuid);
        orderItem.setQuantity(orderItemInsertDTO.getQuantity());
        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    private Order getOrder(String uuid) {
        return orderRepository.findById(UUID.fromString(uuid))
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    private OrderItem getOrderItem(UUID orderUuid, UUID productUuid) {
        return orderItemRepository.findById_OrderIdAndId_ProductId(orderUuid, productUuid);
    }

    private void updateOrder(Order order, OrderInsertDTO orderInsertDTO) {
        ifClientNotExistsThrowsException(orderInsertDTO.getClient_id());
        if(orderInsertDTO.getOrderStatus() == OrderStatus.PAID && order.getPayment() == null)
            throw new NotPaidException("Not paid yet");
        if(order.getPayment() != null && orderInsertDTO.getOrderStatus() == OrderStatus.WAITING_PAYMENT)
            throw new AlreadyPaidException("Already paid, unable to update order status to WAITING_PAYMENT");

        order.setOrderStatus(orderInsertDTO.getOrderStatus());
        order.setClient(userRepository.getReferenceById(UUID.fromString(orderInsertDTO.getClient_id())));
    }

    private void ifProductNotExistsThrowsException(String productUuid) {
        if(!productRepository.existsById(UUID.fromString(productUuid)))
            throw new EntityNotFoundException("Product not found");
    }

    private void ifClientNotExistsThrowsException(String clientUuid) {
        if (!userRepository.existsById(UUID.fromString(clientUuid)))
            throw new EntityNotFoundException("Client not found");
    }

    private void ifOrderIsAlreadyPaidThrowsException(Order order) {
        if(order.getPayment() != null)
            throw new AlreadyPaidException("Already paid, unable to do changes in this order item");
    }

    private void ifOrderItemNotExistsThrowsException(UUID orderUuid, UUID productUuid) {
        if(!orderItemRepository.existsById_OrderIdAndId_ProductId(orderUuid, productUuid))
            throw new EntityNotFoundException("Order item not found");
    }

    private boolean isContextUserAdmin() {
        User contextUser = getContextUser();
        return contextUser.getRoleNames().contains(RoleName.ROLE_ADMIN.toString());
    }

    private void ifUserIsNotSameThrowsException(User userOne, User userTwo) {
        String userOneEmail = userOne.getEmail();
        String userTwoEmail = userTwo.getEmail();
        if(!userOneEmail.equals(userTwoEmail))
            throw new AccessDeniedException("Access denied");
    }
}