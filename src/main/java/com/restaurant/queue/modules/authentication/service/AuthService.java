package com.restaurant.queue.modules.authentication.service;

import com.restaurant.queue.modules.authentication.model.User;
import com.restaurant.queue.modules.authentication.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    public boolean isAdmin() {
        return getCurrentUser()
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
    }

    /**
     * Resolves the restaurant ID for the current authenticated user.
     * - Staff is strictly restricted to their assigned restaurantId.
     * - Admin can view a specified targetRestaurantId, or defaults to targetRestaurantId or 1L.
     */
    public Long resolveRestaurantId(Long requestedRestaurantId) {
        User user = getCurrentUser().orElseThrow(() -> new AccessDeniedException("User is not authenticated"));

        if ("STAFF".equalsIgnoreCase(user.getRole()) || "ROLE_STAFF".equalsIgnoreCase(user.getRole())) {
            Long staffRestaurantId = user.getRestaurantId();
            if (staffRestaurantId == null) {
                throw new AccessDeniedException("Staff member has no assigned restaurant.");
            }
            if (requestedRestaurantId != null && !Objects.equals(requestedRestaurantId, staffRestaurantId)) {
                throw new AccessDeniedException("Access denied: You are not authorized for this restaurant.");
            }
            return staffRestaurantId;
        }

        // For ADMIN:
        if (requestedRestaurantId != null) {
            return requestedRestaurantId;
        }
        return user.getRestaurantId() != null ? user.getRestaurantId() : 1L;
    }

    /**
     * Validates that the current user has access to the given restaurantId.
     */
    public void validateRestaurantAccess(Long targetRestaurantId) {
        resolveRestaurantId(targetRestaurantId);
    }
}
