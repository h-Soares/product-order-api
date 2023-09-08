package com.soaresdev.productorderapi.utils;

import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {
    private Utils() {
    }

    public static User getContextUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean isContextUserAdmin() {
        User contextUser = getContextUser();
        return contextUser.getRoleNames().contains(RoleName.ROLE_ADMIN.toString());
    }

    public static void ifUserIsNotSameThrowsException(User userOne, User userTwo) {
        String userOneEmail = userOne.getEmail();
        String userTwoEmail = userTwo.getEmail();
        if(!userOneEmail.equals(userTwoEmail))
            throw new AccessDeniedException("Access denied");
    }
}