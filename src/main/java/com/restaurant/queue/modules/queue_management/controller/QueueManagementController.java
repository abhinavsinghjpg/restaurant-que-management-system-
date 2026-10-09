package com.restaurant.queue.modules.queue_management.controller;

import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.queue_management.service.QueueManagementService;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
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

    public QueueManagementController(QueueManagementService queueManagementService,
                                      TableManagementService tableManagementService) {
        this.queueManagementService = queueManagementService;
        this.tableManagementService = tableManagementService;
    }

    /** Show the live queue management page. */
    @GetMapping
    public String showQueue(Model model) {
        List<QueueEntry> activeQueue = queueManagementService.getActiveQueue();
        List<RestaurantTable> availableTables = tableManagementService.getAllTables();

        // Find currently called entry (if any)
        Optional<QueueEntry> currentlyCalled = activeQueue.stream()
                .filter(q -> q.getStatus().name().equals("CALLED"))
                .findFirst();

        model.addAttribute("queue", activeQueue);
        model.addAttribute("currentlyCalled", currentlyCalled.orElse(null));
        model.addAttribute("availableTables", availableTables);
        model.addAttribute("waitingCount", activeQueue.stream()
                .filter(q -> q.getStatus().name().equals("WAITING")).count());
        return "admin/queue";
    }

    /** Call the next waiting customer. */
    @PostMapping("/next")
    public String callNext(RedirectAttributes redirectAttributes) {
        Optional<QueueEntry> called = queueManagementService.callNext();
        if (called.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Called: " + called.get().getCustomerName() + " (" + called.get().getTokenNumber() + ")");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "No customers waiting in queue.");
        }
        return "redirect:/admin/queue";
    }

    /** Seat a customer (optionally with a table). */
    @PostMapping("/{id}/seat")
    public String seatCustomer(@PathVariable Long id,
                                @RequestParam(required = false) Long tableId,
                                RedirectAttributes redirectAttributes) {
        queueManagementService.seatCustomer(id, tableId);
        redirectAttributes.addFlashAttribute("successMessage", "Customer seated successfully.");
        return "redirect:/admin/queue";
    }

    /** Skip a customer. */
    @PostMapping("/{id}/skip")
    public String skipCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        queueManagementService.skipCustomer(id);
        redirectAttributes.addFlashAttribute("successMessage", "Customer skipped.");
        return "redirect:/admin/queue";
    }
}
