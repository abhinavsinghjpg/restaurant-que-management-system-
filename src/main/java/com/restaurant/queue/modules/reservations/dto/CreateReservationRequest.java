package com.restaurant.queue.modules.reservations.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateReservationRequest {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String customerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobile;

    @NotNull(message = "Party size is required")
    @Min(value = 1, message = "Party size must be at least 1")
    @Max(value = 20, message = "Party size cannot exceed 20")
    private Integer partySize;

    @NotNull(message = "Reservation date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reservationDate;

    @NotNull(message = "Reservation time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime reservationTime;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    public CreateReservationRequest() {
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

    public LocalTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalTime reservationTime) { this.reservationTime = reservationTime; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
