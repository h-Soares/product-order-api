package com.soaresdev.productorderapi.entities;

import com.soaresdev.productorderapi.entities.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_role")
public class Role implements Serializable, GrantedAuthority {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer roleNameCode;

    public Role() {
    }

    public Role(Integer roleNameCode) {
        this.roleNameCode = roleNameCode;
    }

    @Override
    public String getAuthority() {
        return RoleName.valueOf(roleNameCode).toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRoleNameCode() {
        return roleNameCode;
    }

    public void setRoleNameCode(RoleName roleName) {
        this.roleNameCode = roleName.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(roleNameCode, role.roleNameCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleNameCode);
    }
}