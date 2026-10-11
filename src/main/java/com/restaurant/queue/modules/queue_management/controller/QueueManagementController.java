package com.restaurant.queue.modules.queue_management.controller;

import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.queue_management.service.QueueManagementService;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
import com.restaurant.queue.modules.authentication.service.AuthService;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Staff-facing queue management controller.
 * All routes require authentication (enforced by SecurityConfig).
 */
@Controller
@RequestMapping("/admin/queue")
public class QueueManagementController {

    private final QueueManagementService queueManagementService;
    private final TableManagementService tableManagementService;
    private final AuthService authService;
    private final RestaurantRepository restaurantRepository;

    public QueueManagementController(QueueManagementService queueManagementService,
                                   TableManagementService tableManagementService,
                                   AuthService authService,
                                   RestaurantRepository restaurantRepository) {
        this.queueManagementService = queueManagementService;
        this.tableManagementService = tableManagementService;
        this.authService = authService;
        this.restaurantRepository = restaurantRepository;
    }

    /** Show the live queue management page. */
    @GetMapping
    public String showQueue(@RequestParam(required = false) Long restaurantId, Model model) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        List<QueueEntry> activeQueue = queueManagementService.getActiveQueue(resolvedRestaurantId);
        List<RestaurantTable> availableTables = tableManagementService.getAllTables(resolvedRestaurantId);

        // Find currently called entry (if any)
        Optional<QueueEntry> currentlyCalled = activeQueue.stream()
                .filter(q -> q.getStatus().name().equals("CALLED"))
                .findFirst();

        model.addAttribute("queue", activeQueue);
        model.addAttribute("currentlyCalled", currentlyCalled.orElse(null));
        model.addAttribute("availableTables", availableTables);
        model.addAttribute("waitingCount", activeQueue.stream()
                .filter(q -> q.getStatus().name().equals("WAITING")).count());
        model.addAttribute("currentRestaurantId", resolvedRestaurantId);
        model.addAttribute("isAdmin", authService.isAdmin());
        if (authService.isAdmin()) {
            model.addAttribute("allRestaurants", restaurantRepository.findByActiveTrue());
        }
        return "admin/queue";
    }

    /** Call the next waiting customer. */
    @PostMapping("/next")
    public String callNext(@RequestParam(required = false) Long restaurantId, RedirectAttributes redirectAttributes) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        Optional<QueueEntry> called = queueManagementService.callNext(resolvedRestaurantId);
        if (called.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Called: " + called.get().getCustomerName() + " (" + called.get().getTokenNumber() + ")");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "No customers waiting in queue.");
        }
        return "redirect:/admin/queue" + (authService.isAdmin() && restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }

    /** Seat a customer (optionally with a table). */
    @PostMapping("/{id}/seat")
    public String seatCustomer(@PathVariable Long id,
                                @RequestParam(required = false) Long tableId,
                                @RequestParam(required = false) Long restaurantId,
                                RedirectAttributes redirectAttributes) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        queueManagementService.seatCustomer(resolvedRestaurantId, id, tableId);
        redirectAttributes.addFlashAttribute("successMessage", "Customer seated successfully.");
        return "redirect:/admin/queue" + (authService.isAdmin() && restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }

    /** Skip a customer. */
    @PostMapping("/{id}/skip")
    public String skipCustomer(@PathVariable Long id,
                                @RequestParam(required = false) Long restaurantId,
                                RedirectAttributes redirectAttributes) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        queueManagementService.skipCustomer(resolvedRestaurantId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Customer skipped.");
        return "redirect:/admin/queue" + (authService.isAdmin() && restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }
}
