package com.restaurant.queue.modules.reservations.controller;

import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.common.exception.ResourceNotFoundException;
import com.restaurant.queue.modules.reservations.dto.CreateReservationRequest;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.service.ReservationService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class CustomerReservationController {

    private final ReservationService reservationService;
    private final RestaurantRepository restaurantRepository;

    public CustomerReservationController(ReservationService reservationService,
                                         RestaurantRepository restaurantRepository) {
        this.reservationService = reservationService;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/r/{slug}/reserve")
    public String showReservationForm(@PathVariable String slug, Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with slug: " + slug));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            model.addAttribute("errorTitle", "Reservations Closed");
            model.addAttribute("errorMessage", "This restaurant is currently inactive and not accepting reservations.");
            return "error/error";
        }

        if (!model.containsAttribute("reservationRequest")) {
            CreateReservationRequest req = new CreateReservationRequest();
            req.setReservationDate(LocalDate.now());
            req.setReservationTime(LocalTime.of(19, 0)); // default 7:00 PM
            model.addAttribute("reservationRequest", req);
        }

        model.addAttribute("restaurant", restaurant);
        return "customer/reserve";
    }

    @PostMapping("/r/{slug}/reserve")
    public String processReservation(@PathVariable String slug,
                                     @Valid @ModelAttribute("reservationRequest") CreateReservationRequest request,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with slug: " + slug));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            model.addAttribute("errorTitle", "Reservations Closed");
            model.addAttribute("errorMessage", "This restaurant is currently inactive.");
            return "error/error";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurant", restaurant);
            return "customer/reserve";
        }

        try {
            Reservation reservation = reservationService.createReservation(restaurant.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation request submitted successfully!");
            return "redirect:/reservations/" + reservation.getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            bindingResult.rejectValue("reservationTime", "invalid.time", e.getMessage());
            model.addAttribute("restaurant", restaurant);
            return "customer/reserve";
        }
    }

    @GetMapping("/reservations/{id}")
    public String showReservationStatus(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationByIdPublic(id);
        Restaurant restaurant = restaurantRepository.findById(reservation.getRestaurantId())
                .orElse(null);

        model.addAttribute("reservation", reservation);
        model.addAttribute("restaurant", restaurant);
        return "customer/reservation_status";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservationCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.getReservationByIdPublic(id);
        if (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservationService.updateStatus(id, ReservationStatus.CANCELLED, reservation.getRestaurantId());
            redirectAttributes.addFlashAttribute("successMessage", "Your reservation has been cancelled.");
        }
        return "redirect:/reservations/" + id;
    }
}
