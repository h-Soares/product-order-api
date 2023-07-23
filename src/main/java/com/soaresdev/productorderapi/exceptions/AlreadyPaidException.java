package com.soaresdev.productorderapi.exceptions;

import java.io.Serial;

public class AlreadyPaidException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AlreadyPaidException(String message) {
        super(message);
    }
}