package com.restaurant.queue.modules.dashboard.controller;

import com.restaurant.queue.modules.dashboard.dto.DashboardStatsResponse;
import com.restaurant.queue.modules.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin dashboard controller — shows daily statistics.
 */
@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        DashboardStatsResponse stats = dashboardService.getStats();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    /** Redirect /admin to /admin/dashboard */
    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
}
