package com.restaurant.queue.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handler for all controllers.
 * Returns user-friendly error pages instead of raw stack traces.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(QueueEntryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleQueueNotFound(QueueEntryNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Queue Entry Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidTransition(InvalidStatusTransitionException ex, Model model) {
        model.addAttribute("errorTitle", "Invalid Action");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(TableNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleTableUnavailable(TableNotAvailableException ex, Model model) {
        model.addAttribute("errorTitle", "Table Not Available");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(MethodArgumentNotValidException ex, Model model) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        model.addAttribute("errorTitle", "Validation Failed");
        model.addAttribute("validationErrors", errors);
        model.addAttribute("errorMessage", "Please correct the errors below and try again.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Something Went Wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/error";
    }
}
