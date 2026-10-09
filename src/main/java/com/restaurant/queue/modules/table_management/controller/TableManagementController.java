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

    public TableManagementController(TableManagementService tableManagementService) {
        this.tableManagementService = tableManagementService;
    }

    /** Show all restaurant tables. */
    @GetMapping
    public String showTables(Model model) {
        List<RestaurantTable> tables = tableManagementService.getAllTables();
        model.addAttribute("tables", tables);
        return "admin/tables";
    }

    /** Assign (occupy) a table manually. */
    @PostMapping("/{id}/assign")
    public String assignTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tableManagementService.assignTable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Table marked as occupied.");
        return "redirect:/admin/tables";
    }

    /** Release a table (make it available). */
    @PostMapping("/{id}/release")
    public String releaseTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tableManagementService.releaseTable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Table released and now available.");
        return "redirect:/admin/tables";
    }
}
