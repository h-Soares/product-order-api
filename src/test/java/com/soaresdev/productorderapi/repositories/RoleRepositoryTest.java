package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DataJpaTest
@ActiveProfiles(value = "test")
class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    private Role role;

    @BeforeEach
    void setup() {
        role = new Role(RoleName.ROLE_ADMIN.getCode());
        role = roleRepository.save(role);
    }

    @Order(1)
    @Test
    void shouldFindByRoleNameCode() {
        Role roleFound = roleRepository.findByRoleNameCode(RoleName.ROLE_ADMIN.getCode());
        assertNotNull(roleFound);
        assertEquals(role, roleFound);
    }

    @Order(2)
    @Test
    void shouldNotFindByRoleNameCode() {
        Role roleFound = roleRepository.findByRoleNameCode(RoleName.ROLE_USER.getCode());
        assertNull(roleFound);
    }
}