package com.restaurant.queue.modules.authentication.controller;

import com.restaurant.queue.modules.authentication.model.User;
import com.restaurant.queue.modules.authentication.repository.UserRepository;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminStaffController(UserRepository userRepository,
                                RestaurantRepository restaurantRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listStaff(Model model) {
        List<User> users = userRepository.findAll();
        List<Restaurant> restaurants = restaurantRepository.findByActiveTrue();
        model.addAttribute("users", users);
        model.addAttribute("restaurants", restaurants);
        return "admin/staff";
    }

    @PostMapping("/create")
    public String createStaff(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam(required = false) Long restaurantId,
                              @RequestParam(defaultValue = "STAFF") String role,
                              RedirectAttributes redirectAttributes) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username and password are required.");
            return "redirect:/admin/staff";
        }

        if (userRepository.existsByUsername(username.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + username + "' is already taken.");
            return "redirect:/admin/staff";
        }

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase());
        newUser.setRestaurantId(restaurantId);
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("successMessage", "User '" + newUser.getUsername() + "' created successfully.");
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Protect current admin from self-deactivation
            user.setActive(!Boolean.TRUE.equals(user.getActive()));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getUsername() + "' status changed to " + (user.getActive() ? "Active" : "Deactivated"));
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/assign-restaurant")
    public String assignRestaurant(@PathVariable Long id,
                                   @RequestParam Long restaurantId,
                                   RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRestaurantId(restaurantId);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Assigned " + user.getUsername() + " to restaurant ID " + restaurantId);
        }
        return "redirect:/admin/staff";
    }
}
