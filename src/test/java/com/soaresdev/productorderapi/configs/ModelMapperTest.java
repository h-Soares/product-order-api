package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.dtos.insertDTOs.OrderInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.PaymentInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.Payment;
import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.OrderStatus;
import com.soaresdev.productorderapi.entities.enums.PaymentType;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.repositories.OrderRepository;
import com.soaresdev.productorderapi.repositories.RoleRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelMapperTest {
    private ModelMapper modelMapper;

    @InjectMocks
    private ModelMapperConfig modelMapperConfig;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final String ENCRYPTED_PASSWORD = "$123$encryptedPassword";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        modelMapper = modelMapperConfig.modelMapper();
    }

    @Test
    void paymentConverterTest() {
        PaymentInsertDTO paymentInsertDTO = new PaymentInsertDTO();
        paymentInsertDTO.setPaymentType(PaymentType.PIX);
        paymentInsertDTO.setOrder_id(UUID.randomUUID().toString());
        Order order = new Order(Instant.now(), OrderStatus.DELIVERED, new User());

        when(orderRepository.getReferenceById(any(UUID.class))).thenReturn(order);
        Payment payment = modelMapper.map(paymentInsertDTO, Payment.class);

        assertNotNull(payment);
        assertEquals(PaymentType.PIX.getCode(), payment.getPaymentType());
        assertNotNull(payment.getOrder());
        assertEquals(OrderStatus.PAID.getCode(), payment.getOrder().getOrderStatus());
        verify(orderRepository, times(1)).getReferenceById(any(UUID.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void orderConverterTest() {
        OrderInsertDTO orderInsertDTO = new OrderInsertDTO();
        orderInsertDTO.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        orderInsertDTO.setClient_id(UUID.randomUUID().toString());
        User user = getTestUser();

        when(userRepository.getReferenceById(any(UUID.class))).thenReturn(user);
        Order order = modelMapper.map(orderInsertDTO, Order.class);

        assertNotNull(order);
        assertEquals(OrderStatus.WAITING_PAYMENT.getCode(), order.getOrderStatus());
        assertNotNull(order.getClient());
        verify(userRepository, times(1)).getReferenceById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUserConverter() {
        UserInsertDTO userInsertDTO = getTestUserInsertDTO();
        Role role = new Role(RoleName.ROLE_USER.getCode());

        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
        when(roleRepository.findByRoleNameCode(anyInt())).thenReturn(role);
        User user = modelMapper.map(userInsertDTO, User.class, "createUserConverter");

        assertNotNull(user);
        assertEquals(userInsertDTO.getName(), user.getName());
        assertEquals(userInsertDTO.getEmail(), user.getEmail());
        assertEquals(userInsertDTO.getPhone(), user.getPhone());
        assertEquals(ENCRYPTED_PASSWORD, user.getPassword());
        assertFalse(user.getRoles().isEmpty());
        assertEquals(1, user.getRoles().size());
        assertEquals(role.getRoleNameCode(), user.getRoles().get(0).getRoleNameCode());
        verify(bCryptPasswordEncoder, times(1)).encode(anyString());
        verifyNoMoreInteractions(bCryptPasswordEncoder);
        verify(roleRepository, times(1)).findByRoleNameCode(anyInt());
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void updateUserConverterTest() {
        User user = getTestUser();
        UserInsertDTO userInsertDTO = getTestUserInsertDTO();

        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
        modelMapper.map(userInsertDTO, user, "updateUserConverter");

        assertNotNull(user);
        assertEquals(userInsertDTO.getName(), user.getName());
        assertEquals(userInsertDTO.getEmail(), user.getEmail());
        assertEquals(userInsertDTO.getPhone(), user.getPhone());
        assertEquals(ENCRYPTED_PASSWORD, user.getPassword());
        verify(bCryptPasswordEncoder, times(1)).encode(anyString());
        verifyNoMoreInteractions(bCryptPasswordEncoder);
    }

    private User getTestUser() {
        return new User("test", "test@gmail.com", "test", "test");
    }

    private UserInsertDTO getTestUserInsertDTO() {
        UserInsertDTO testUserInsertDTO = new UserInsertDTO();
        testUserInsertDTO.setName("Testing");
        testUserInsertDTO.setEmail("testing@gmail.com");
        testUserInsertDTO.setPhone("16123456789");
        testUserInsertDTO.setPassword("StrongPassword123");
        return testUserInsertDTO;
    }
}