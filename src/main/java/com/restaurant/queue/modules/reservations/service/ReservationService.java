package com.restaurant.queue.modules.reservations.service;

import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.ResourceNotFoundException;
import com.restaurant.queue.modules.reservations.dto.CreateReservationRequest;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.repository.ReservationRepository;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantRepository restaurantRepository,
                              RestaurantTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
    }

    @Transactional
    public Reservation createReservation(Long restaurantId, CreateReservationRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        if (!Boolean.TRUE.equals(restaurant.getActive())) {
            throw new IllegalStateException("Restaurant is currently inactive and not accepting reservations.");
        }

        LocalDateTime bookingDateTime = LocalDateTime.of(request.getReservationDate(), request.getReservationTime());
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation time must be in the future.");
        }

        Reservation reservation = new Reservation(
                restaurantId,
                request.getCustomerName().trim(),
                request.getMobile().trim(),
                request.getPartySize(),
                bookingDateTime
        );
        reservation.setSpecialRequests(request.getSpecialRequests() != null ? request.getSpecialRequests().trim() : null);
        reservation.setStatus(ReservationStatus.PENDING);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservations(Long restaurantId, LocalDate filterDate, ReservationStatus filterStatus) {
        if (filterDate != null) {
            LocalDateTime startOfDay = filterDate.atStartOfDay();
            LocalDateTime endOfDay = filterDate.atTime(LocalTime.MAX);

            if (filterStatus != null) {
                return reservationRepository.findByRestaurantIdAndReservationTimeBetweenAndStatusOrderByReservationTimeAsc(
                        restaurantId, startOfDay, endOfDay, filterStatus);
            } else {
                return reservationRepository.findByRestaurantIdAndReservationTimeBetweenOrderByReservationTimeAsc(
                        restaurantId, startOfDay, endOfDay);
            }
        } else {
            if (filterStatus != null) {
                return reservationRepository.findByRestaurantIdAndStatusOrderByReservationTimeAsc(restaurantId, filterStatus);
            } else {
                return reservationRepository.findByRestaurantIdOrderByReservationTimeAsc(restaurantId);
            }
        }
    }

    public Reservation getReservationById(Long reservationId, Long restaurantId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (restaurantId != null && !restaurantId.equals(reservation.getRestaurantId())) {
            throw new AccessDeniedException("Unauthorized access to another restaurant's reservation.");
        }

        return reservation;
    }

    public Reservation getReservationByIdPublic(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));
    }

    @Transactional
    public Reservation updateStatus(Long reservationId, ReservationStatus newStatus, Long restaurantId) {
        Reservation reservation = getReservationById(reservationId, restaurantId);

        reservation.setStatus(newStatus);
        if (newStatus == ReservationStatus.CANCELLED || newStatus == ReservationStatus.NO_SHOW) {
            reservation.setAssignedTableId(null);
        } else if (newStatus == ReservationStatus.SEATED && reservation.getAssignedTableId() != null) {
            tableRepository.findById(reservation.getAssignedTableId()).ifPresent(t -> {
                t.setStatus(TableStatus.OCCUPIED);
                tableRepository.save(t);
            });
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation assignTable(Long reservationId, Long tableId, Long restaurantId) {
        Reservation reservation = getReservationById(reservationId, restaurantId);

        if (tableId == null) {
            reservation.setAssignedTableId(null);
            return reservationRepository.save(reservation);
        }

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with ID: " + tableId));

        if (!restaurantId.equals(table.getRestaurantId())) {
            throw new AccessDeniedException("Table belongs to a different restaurant.");
        }

        if (table.getCapacity() < reservation.getPartySize()) {
            throw new IllegalArgumentException("Table capacity (" + table.getCapacity() +
                    ") is insufficient for party size of " + reservation.getPartySize());
        }

        // Check for conflicting reservations on the same table within 90 minutes
        LocalDateTime slotStart = reservation.getReservationTime().minusMinutes(90);
        LocalDateTime slotEnd = reservation.getReservationTime().plusMinutes(90);

        long overlapping = reservationRepository.countOverlappingTableReservations(
                restaurantId, tableId, reservation.getId(), slotStart, slotEnd);

        if (overlapping > 0) {
            throw new IllegalArgumentException("Table " + table.getTableNumber() +
                    " is already reserved for another customer at this time.");
        }

        reservation.setAssignedTableId(tableId);
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
        }

        return reservationRepository.save(reservation);
    }
}
