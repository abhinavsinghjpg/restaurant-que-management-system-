package com.restaurant.queue.modules.reservations.repository;

import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.modules.reservations.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRestaurantIdOrderByReservationTimeAsc(Long restaurantId);

    List<Reservation> findByRestaurantIdAndReservationTimeBetweenOrderByReservationTimeAsc(
            Long restaurantId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByRestaurantIdAndStatusOrderByReservationTimeAsc(
            Long restaurantId, ReservationStatus status);

    List<Reservation> findByRestaurantIdAndReservationTimeBetweenAndStatusOrderByReservationTimeAsc(
            Long restaurantId, LocalDateTime start, LocalDateTime end, ReservationStatus status);

    long countByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);

    long countByRestaurantIdAndReservationTimeBetween(Long restaurantId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.restaurantId = :restaurantId
          AND r.assignedTableId = :tableId
          AND (:excludeId IS NULL OR r.id != :excludeId)
          AND r.status IN ('CONFIRMED', 'SEATED')
          AND r.reservationTime >= :startTime
          AND r.reservationTime <= :endTime
    """)
    long countOverlappingTableReservations(@Param("restaurantId") Long restaurantId,
                                           @Param("tableId") Long tableId,
                                           @Param("excludeId") Long excludeId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
