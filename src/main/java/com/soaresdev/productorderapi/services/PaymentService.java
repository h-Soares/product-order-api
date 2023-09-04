package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.PaymentDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.exceptions.AlreadyPaidException;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    public Page<PaymentDTO> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentDTO::new);
    }

    public PaymentDTO findByUUID(String uuid) {
        Payment payment = getPayment(uuid);
        User user = payment.getOrder().getClient();

        if(user.getRoleNames().stream().noneMatch(r -> r.equals("ROLE_MANAGER") || r.equals("ROLE_ADMIN"))) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!user.getEmail().equals(email))
                throw new AccessDeniedException("You do not have permission to see this payment");
        }

        return new PaymentDTO(payment);
    }

    @Transactional
    public PaymentDTO insert(PaymentInsertDTO paymentInsertDTO) {
        if(!orderRepository.existsById(UUID.fromString(paymentInsertDTO.getOrder_id())))
            throw new EntityNotFoundException("Order not found");

        if(paymentRepository.existsByOrderId(UUID.fromString(paymentInsertDTO.getOrder_id())))
            throw new AlreadyPaidException("Order already paid");

        User user = orderRepository.getReferenceById(UUID.fromString(paymentInsertDTO.getOrder_id())).getClient();
        if(user.getRoleNames().stream().noneMatch(r -> r.equals("ROLE_MANAGER") || r.equals("ROLE_ADMIN"))) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!user.getEmail().equals(email))
                throw new AccessDeniedException("You do not have permission to pay this order");
        }

        Payment payment = modelMapper.map(paymentInsertDTO, Payment.class);
        payment = paymentRepository.save(payment);
        return new PaymentDTO(payment);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        Payment payment = getPayment(uuid);
        payment.getOrder().setOrderStatus(OrderStatus.WAITING_PAYMENT);
        paymentRepository.deleteByUUID(getPayment(uuid).getId());
    }

    @Transactional
    public PaymentDTO updateByUUID(String uuid, PaymentInsertDTO paymentInsertDTO) {
        Payment payment = getPayment(uuid);
        updatePayment(payment, paymentInsertDTO);
        payment = paymentRepository.save(payment);
        return new PaymentDTO(payment);
    }

    private void updatePayment(Payment payment, PaymentInsertDTO paymentInsertDTO) {
        if(!orderRepository.existsById(UUID.fromString(paymentInsertDTO.getOrder_id())))
            throw new EntityNotFoundException("Order not found");
        if(!UUID.fromString(paymentInsertDTO.getOrder_id()).equals(payment.getOrder().getId()) && paymentRepository.existsByOrderId(UUID.fromString(paymentInsertDTO.getOrder_id())))
            throw new AlreadyPaidException("Order already paid");
        if(UUID.fromString(paymentInsertDTO.getOrder_id()) != payment.getOrder().getId())
            payment.getOrder().setOrderStatus(OrderStatus.WAITING_PAYMENT);

        Order order = orderRepository.getReferenceById(UUID.fromString(paymentInsertDTO.getOrder_id()));
        order.setOrderStatus(OrderStatus.PAID);
        payment.setPaymentType(paymentInsertDTO.getPaymentType());
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
    }

    private Payment getPayment(String uuid) {
        return paymentRepository.findById(UUID.fromString(uuid)).
               orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }
}