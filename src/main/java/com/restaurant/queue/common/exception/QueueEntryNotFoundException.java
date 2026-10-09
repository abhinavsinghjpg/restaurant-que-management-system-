package com.restaurant.queue.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class QueueEntryNotFoundException extends RuntimeException {

    public QueueEntryNotFoundException(String message) {
        super(message);
    }
}
