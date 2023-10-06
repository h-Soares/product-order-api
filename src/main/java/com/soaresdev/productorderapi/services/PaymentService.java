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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static com.soaresdev.productorderapi.utils.Utils.*;

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
        User contextUser = getContextUser();
        if(contextUser.getRoleNames().stream().noneMatch(r -> r.equals("ROLE_MANAGER") || r.equals("ROLE_ADMIN")))
            ifUserIsNotSameThrowsException(payment.getOrder().getClient(), contextUser);

        return new PaymentDTO(payment);
    }

    @Transactional
    public PaymentDTO insert(PaymentInsertDTO paymentInsertDTO) {
        UUID insertDTOOrderUuid = UUID.fromString(paymentInsertDTO.getOrder_id());
        ifPaymentOrderNotExistsThrowsException(insertDTOOrderUuid);

        if(paymentRepository.existsByOrderId(insertDTOOrderUuid))
            throw new AlreadyPaidException("Order already paid");

        User contextUser = getContextUser();
        if(contextUser.getRoleNames().stream().noneMatch(r -> r.equals("ROLE_MANAGER") || r.equals("ROLE_ADMIN"))) {
            User orderClient = orderRepository.getReferenceById(insertDTOOrderUuid).getClient();
            ifUserIsNotSameThrowsException(orderClient, contextUser);
        }

        Payment payment = modelMapper.map(paymentInsertDTO, Payment.class);
        payment = paymentRepository.save(payment);
        return new PaymentDTO(payment);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        Payment payment = getPayment(uuid);
        payment.getOrder().setOrderStatus(OrderStatus.WAITING_PAYMENT);
        paymentRepository.deleteByUUID(payment.getId());
    }

    @Transactional
    public PaymentDTO updateByUUID(String uuid, PaymentInsertDTO paymentInsertDTO) {
        Payment payment = getPayment(uuid);
        updatePayment(payment, paymentInsertDTO);
        payment = paymentRepository.save(payment);
        return new PaymentDTO(payment);
    }

    private void updatePayment(Payment payment, PaymentInsertDTO paymentInsertDTO) {
        UUID insertDTOOrderUuid = UUID.fromString(paymentInsertDTO.getOrder_id());
        ifPaymentOrderNotExistsThrowsException(insertDTOOrderUuid);
        if(!insertDTOOrderUuid.equals(payment.getOrder().getId()) && paymentRepository.existsByOrderId(insertDTOOrderUuid))
            throw new AlreadyPaidException("Order already paid");
        if(insertDTOOrderUuid != payment.getOrder().getId())
            payment.getOrder().setOrderStatus(OrderStatus.WAITING_PAYMENT);

        Order order = orderRepository.getReferenceById(insertDTOOrderUuid);
        order.setOrderStatus(OrderStatus.PAID);
        payment.setPaymentType(paymentInsertDTO.getPaymentType());
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
    }

    private Payment getPayment(String uuid) {
        return paymentRepository.findById(UUID.fromString(uuid)).
               orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }

    private void ifPaymentOrderNotExistsThrowsException(UUID paymentOrderUuid) {
        if(!orderRepository.existsById(paymentOrderUuid))
            throw new EntityNotFoundException("Order not found");
    }
}