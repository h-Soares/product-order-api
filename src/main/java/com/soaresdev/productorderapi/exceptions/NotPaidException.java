package com.soaresdev.productorderapi.exceptions;

import java.io.Serial;

public class NotPaidException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotPaidException(String message) {
        super(message);
    }
}