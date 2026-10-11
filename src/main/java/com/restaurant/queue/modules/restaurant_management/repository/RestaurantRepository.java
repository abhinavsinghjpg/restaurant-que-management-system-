package com.restaurant.queue.modules.restaurant_management.repository;

import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findBySlug(String slug);

    Optional<Restaurant> findBySlugAndActiveTrue(String slug);

    List<Restaurant> findAllByOrderByCreatedAtDesc();

    List<Restaurant> findByActiveTrue();

    boolean existsBySlug(String slug);
}
