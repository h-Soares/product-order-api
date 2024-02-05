package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByRoleNameCode(Integer code);

    boolean existsByRoleNameCode(Integer code);
}