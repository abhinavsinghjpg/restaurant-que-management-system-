package com.restaurant.queue.modules.reservations.controller;

import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.modules.authentication.service.AuthService;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.service.ReservationService;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final TableManagementService tableManagementService;
    private final AuthService authService;
    private final RestaurantRepository restaurantRepository;

    public AdminReservationController(ReservationService reservationService,
                                    TableManagementService tableManagementService,
                                    AuthService authService,
                                    RestaurantRepository restaurantRepository) {
        this.reservationService = reservationService;
        this.tableManagementService = tableManagementService;
        this.authService = authService;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public String listReservations(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            Model model) {

        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        List<Reservation> reservations = reservationService.getReservations(resolvedId, date, status);
        List<RestaurantTable> tables = tableManagementService.getAllTables(resolvedId);

        model.addAttribute("reservations", reservations);
        model.addAttribute("tables", tables);
        model.addAttribute("currentRestaurantId", resolvedId);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", ReservationStatus.values());
        model.addAttribute("isAdmin", authService.isAdmin());
        if (authService.isAdmin()) {
            model.addAttribute("allRestaurants", restaurantRepository.findByActiveTrue());
        }

        return "admin/reservations";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus newStatus,
            @RequestParam(required = false) Long restaurantId,
            RedirectAttributes redirectAttributes) {

        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        reservationService.updateStatus(id, newStatus, resolvedId);
        redirectAttributes.addFlashAttribute("successMessage", "Reservation status updated to " + newStatus);
        return "redirect:/admin/reservations" + (restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }

    @PostMapping("/{id}/assign-table")
    public String assignTable(
            @PathVariable Long id,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) Long restaurantId,
            RedirectAttributes redirectAttributes) {

        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        try {
            reservationService.assignTable(id, tableId, resolvedId);
            redirectAttributes.addFlashAttribute("successMessage", tableId != null
                    ? "Table assigned successfully and reservation confirmed!"
                    : "Table unassigned.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/reservations" + (restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }
}
