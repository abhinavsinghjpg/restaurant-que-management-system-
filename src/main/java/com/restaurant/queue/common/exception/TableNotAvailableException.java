package com.restaurant.queue.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TableNotAvailableException extends RuntimeException {

    public TableNotAvailableException(String message) {
        super(message);
    }
}
