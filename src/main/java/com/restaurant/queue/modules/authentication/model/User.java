package com.restaurant.queue.modules.authentication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a staff/admin user who can log in.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;  // ROLE_ADMIN or ROLE_STAFF

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
