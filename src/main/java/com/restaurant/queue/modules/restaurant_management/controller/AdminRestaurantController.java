package com.restaurant.queue.modules.restaurant_management.controller;

import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurants")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantController {

    private final RestaurantRepository restaurantRepository;

    public AdminRestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public String listRestaurants(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("restaurants", restaurants);
        return "admin/restaurants";
    }

    @PostMapping("/create")
    public String createRestaurant(@RequestParam String name,
                                   @RequestParam(required = false) String slug,
                                   RedirectAttributes redirectAttributes) {
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Restaurant name is required.");
            return "redirect:/admin/restaurants";
        }

        String targetSlug = (slug != null && !slug.trim().isEmpty())
                ? slug.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-")
                : name.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");

        if (restaurantRepository.existsBySlug(targetSlug)) {
            targetSlug = targetSlug + "-" + System.currentTimeMillis() % 10000;
        }

        Restaurant restaurant = new Restaurant(targetSlug, name.trim());
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setActive(true);
        restaurantRepository.save(restaurant);

        redirectAttributes.addFlashAttribute("successMessage", "Restaurant created: " + restaurant.getName() + " (slug: " + restaurant.getSlug() + ")");
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant != null) {
            restaurant.setActive(!Boolean.TRUE.equals(restaurant.getActive()));
            restaurantRepository.save(restaurant);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Restaurant '" + restaurant.getName() + "' is now " + (restaurant.getActive() ? "Active" : "Inactive"));
        }
        return "redirect:/admin/restaurants";
    }
}
