package com.soaresdev.productorderapi.dtos.insertDTOs;

import com.soaresdev.productorderapi.entities.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

public class UserRoleInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Role name can not be null")
    private RoleName roleName;

    public UserRoleInsertDTO() {
    }

    public UserRoleInsertDTO(RoleName roleName) {
        this.roleName = roleName;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }
}