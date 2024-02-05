package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles(value = "test")
@Import(RoleConfig.class)
class RoleConfigTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void initializeRoles() {
        for(RoleName roleName : RoleName.values())
            assertTrue(roleRepository.existsByRoleNameCode(roleName.getCode()));
    }
}