package com.soaresdev.productorderapi.dtos.insertDTOs;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

public class UserInsertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String NAME_REGEX = "^(?!.*[#@!0-9])[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()\\-_=+{}\\[\\]|\\\\:;\"'<>,.?\\/`~]{6,}$";
    private static final String UUID_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[1-5][a-fA-F0-9]{3}-[89aAbB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";
    private static final String PHONE_REGEX = "^$|^\\(?([0-9]{3})\\)?([ .-]?)([0-9]{3})\\2([0-9]{4})$|^([0-9]{11})$";

    @NotNull(message = "Name can not be null")
    @Size(min = 2, max = 65, message = "Name must be between 2 and 65 characters")
    @Pattern(regexp = NAME_REGEX, message = "Invalid name")
    private String name;

    @NotBlank(message = "Email can not be null or empty")
    @Email(message = "Invalid email")
    private String email;

    @NotNull(message = "Password can not be null")
    @Pattern(regexp = PASSWORD_REGEX, message =
            "Password must contain at least 6 characters, with at least one number")
    private String password;

    @NotNull(message = "Phone can not be null")
    @Pattern(regexp = PHONE_REGEX, message = "Invalid phone")
    private String phone;

    public UserInsertDTO() {
    }

    public UserInsertDTO(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}