package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DataJpaTest
@ActiveProfiles(value = "test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;
    private Role role;

    @BeforeEach
    void setup() {
        role = new Role(RoleName.ROLE_USER.getCode());
        role = roleRepository.save(role);

        user = new User("test", "test@email.com", "test", "test");
        user.getRoles().add(role);
        user = userRepository.save(user);
    }

    @Order(1)
    @Test
    void shouldUserExistsByEmail() {
        boolean result = userRepository.existsByEmail("test@email.com");
        assertTrue(result);
    }

    @Order(2)
    @Test
    void shouldUserNotExistsByEmail() {
        boolean result = userRepository.existsByEmail("testingifnotexists@email.com");
        assertFalse(result);
    }

    @Order(3)
    @Test
    void shouldFindUserByEmailWithEagerRoles() {
        Optional<User> user = userRepository.findByEmailWithEagerRoles("test@email.com");
        assertTrue(user.isPresent());
        assertFalse(user.get().getRoles().isEmpty());
        assertEquals(1, user.get().getRoles().size());
        assertEquals(RoleName.ROLE_USER.getCode(), user.get().getRoles().get(0).getRoleNameCode());
    }

    @Order(4)
    @Test
    void shouldNotFindUserByEmailWithEagerRoles() {
        Optional<User> user = userRepository.findByEmailWithEagerRoles("testingifnotexists@email.com");
        assertTrue(user.isEmpty());
    }


    @Order(5)
    @Test
    void shouldFindAllUsersWithPage() {
        User user2 = new User("test2", "test2@gmail.com", "test2", "test2");
        user2.getRoles().add(role);
        user2 = userRepository.save(user2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(user, result.getContent().get(0));
        assertEquals(user2, result.getContent().get(1));
    }
}
