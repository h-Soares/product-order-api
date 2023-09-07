package com.soaresdev.productorderapi.utils;

import com.soaresdev.productorderapi.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {
    private Utils() {
    }

    public static User getContextUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}