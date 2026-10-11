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
    private final com.restaurant.queue.modules.authentication.service.AuthService authService;
    private final com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository;

    public DashboardController(DashboardService dashboardService,
                               com.restaurant.queue.modules.authentication.service.AuthService authService,
                               com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository) {
        this.dashboardService = dashboardService;
        this.authService = authService;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@org.springframework.web.bind.annotation.RequestParam(required = false) Long restaurantId, Model model) {
        Long resolvedRestaurantId = authService.resolveRestaurantId(restaurantId);
        DashboardStatsResponse stats = dashboardService.getStats(resolvedRestaurantId);
        model.addAttribute("stats", stats);
        model.addAttribute("currentRestaurantId", resolvedRestaurantId);
        model.addAttribute("isAdmin", authService.isAdmin());
        if (authService.isAdmin()) {
            model.addAttribute("allRestaurants", restaurantRepository.findByActiveTrue());
        }
        return "admin/dashboard";
    }

    /** Redirect /admin to /admin/dashboard */
    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
}
