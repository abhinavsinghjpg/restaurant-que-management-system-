package com.restaurant.queue.modules.dashboard.controller;

import com.restaurant.queue.modules.authentication.service.AuthService;
import com.restaurant.queue.modules.dashboard.dto.AnalyticsResponseDto;
import com.restaurant.queue.modules.dashboard.service.AnalyticsService;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;
    private final RestaurantRepository restaurantRepository;

    public AnalyticsController(AnalyticsService analyticsService,
                               AuthService authService,
                               RestaurantRepository restaurantRepository) {
        this.analyticsService = analyticsService;
        this.authService = authService;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/admin/analytics")
    public String showAnalyticsPage(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "7days") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        AnalyticsResponseDto analytics = analyticsService.getAnalytics(resolvedId, range, startDate, endDate);

        model.addAttribute("analytics", analytics);
        model.addAttribute("currentRestaurantId", resolvedId);
        model.addAttribute("activeRange", range);
        model.addAttribute("isAdmin", authService.isAdmin());
        if (authService.isAdmin()) {
            model.addAttribute("allRestaurants", restaurantRepository.findByActiveTrue());
        }

        return "admin/analytics";
    }

    @GetMapping("/api/admin/analytics/data")
    @ResponseBody
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsData(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "7days") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long resolvedId = authService.resolveRestaurantId(restaurantId);
        AnalyticsResponseDto data = analyticsService.getAnalytics(resolvedId, range, startDate, endDate);
        return ResponseEntity.ok(data);
    }
}
