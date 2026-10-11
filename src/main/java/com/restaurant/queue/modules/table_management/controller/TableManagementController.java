package com.restaurant.queue.modules.table_management.controller;

import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Staff-facing table management controller.
 */
@Controller
@RequestMapping("/admin/tables")
public class TableManagementController {

    private final TableManagementService tableManagementService;
    private final com.restaurant.queue.modules.authentication.service.AuthService authService;
    private final com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository;

    public TableManagementController(TableManagementService tableManagementService,
                                   com.restaurant.queue.modules.authentication.service.AuthService authService,
                                   com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository) {
        this.tableManagementService = tableManagementService;
        this.authService = authService;
        this.restaurantRepository = restaurantRepository;
    }

    /** Show all restaurant tables. */
    @GetMapping
    public String showTables(@RequestParam(required = false) Long restaurantId, Model model) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        List<RestaurantTable> tables = tableManagementService.getAllTables(resolvedRestaurantId);
        model.addAttribute("tables", tables);
        model.addAttribute("currentRestaurantId", resolvedRestaurantId);
        model.addAttribute("isAdmin", authService.isAdmin());
        if (authService.isAdmin()) {
            model.addAttribute("allRestaurants", restaurantRepository.findByActiveTrue());
        }
        return "admin/tables";
    }

    /** Assign (occupy) a table manually. */
    @PostMapping("/{id}/assign")
    public String assignTable(@PathVariable Long id,
                               @RequestParam(required = false) Long restaurantId,
                               RedirectAttributes redirectAttributes) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        tableManagementService.assignTable(resolvedRestaurantId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Table marked as occupied.");
        return "redirect:/admin/tables" + (authService.isAdmin() && restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }

    /** Release a table (make it available). */
    @PostMapping("/{id}/release")
    public String releaseTable(@PathVariable Long id,
                                @RequestParam(required = false) Long restaurantId,
                                RedirectAttributes redirectAttributes) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        tableManagementService.releaseTable(resolvedRestaurantId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Table released and now available.");
        return "redirect:/admin/tables" + (authService.isAdmin() && restaurantId != null ? "?restaurantId=" + restaurantId : "");
    }
}
