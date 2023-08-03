package com.soaresdev.productorderapi.entities.enums;

public enum RoleName {
    ROLE_USER(1),
    ROLE_ADMIN(2),
    ROLE_MANAGER(3);

    private final Integer code;
    RoleName(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static RoleName valueOf(Integer code) {
        for(RoleName roleName : RoleName.values()) {
            if(code.equals(roleName.getCode()))
                return roleName;
        }
        throw new IllegalArgumentException("Invalid role name code");
    }
}