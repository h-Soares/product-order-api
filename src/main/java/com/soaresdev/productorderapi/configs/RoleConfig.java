package com.soaresdev.productorderapi.configs;

import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleConfig {
    private final RoleRepository roleRepository;

    public RoleConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initializeRoles() {
        for(RoleName roleName : RoleName.values())
            initializeRoleIfNotExists(roleName);
    }

    private void initializeRoleIfNotExists(RoleName roleName) {
        if(!roleRepository.existsByRoleNameCode(roleName.getCode()))
            roleRepository.save(new Role(roleName.getCode()));
    }
}