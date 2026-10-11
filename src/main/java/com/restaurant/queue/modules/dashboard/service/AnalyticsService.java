package com.restaurant.queue.modules.dashboard.service;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.ReservationStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.ResourceNotFoundException;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.dashboard.dto.AnalyticsResponseDto;
import com.restaurant.queue.modules.reservations.model.Reservation;
import com.restaurant.queue.modules.reservations.repository.ReservationRepository;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AnalyticsService {

    private final QueueEntryRepository queueEntryRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;

    public AnalyticsService(QueueEntryRepository queueEntryRepository,
                            ReservationRepository reservationRepository,
                            RestaurantRepository restaurantRepository,
                            RestaurantTableRepository tableRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.reservationRepository = reservationRepository;
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
    }

    public AnalyticsResponseDto getAnalytics(Long restaurantId, String range, LocalDate customStart, LocalDate customEnd) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        if ("today".equalsIgnoreCase(range)) {
            startDate = LocalDate.now();
        } else if ("7days".equalsIgnoreCase(range)) {
            startDate = LocalDate.now().minusDays(6);
        } else if ("30days".equalsIgnoreCase(range)) {
            startDate = LocalDate.now().minusDays(29);
        } else if ("custom".equalsIgnoreCase(range) && customStart != null && customEnd != null) {
            startDate = customStart;
            endDate = customEnd;
        } else {
            // Default or "all"
            range = "all";
            startDate = LocalDate.of(2024, 1, 1);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        AnalyticsResponseDto dto = new AnalyticsResponseDto();
        dto.setRestaurantId(restaurant.getId());
        dto.setRestaurantName(restaurant.getName());
        dto.setRange(range);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        // Fetch queue entries in range
        List<QueueEntry> entries = queueEntryRepository.findByRestaurantIdAndJoinTimeBetweenOrderByJoinTimeAsc(
                restaurantId, startDateTime, endDateTime);

        dto.setTotalVisits(entries.size());

        long seated = 0;
        long waiting = 0;
        long called = 0;
        long cancelled = 0;
        long skipped = 0;
        long totalGuests = 0;

        int[] hourly = new int[24];
        List<Long> waitMinutesList = new ArrayList<>();

        for (QueueEntry q : entries) {
            if (q.getStatus() == QueueStatus.SEATED) {
                seated++;
                totalGuests += (q.getPartySize() != null ? q.getPartySize() : 1);
                if (q.getJoinTime() != null && q.getSeatedTime() != null) {
                    long wait = Duration.between(q.getJoinTime(), q.getSeatedTime()).toMinutes();
                    if (wait >= 0) waitMinutesList.add(wait);
                }
            } else if (q.getStatus() == QueueStatus.WAITING) {
                waiting++;
            } else if (q.getStatus() == QueueStatus.CALLED) {
                called++;
            } else if (q.getStatus() == QueueStatus.CANCELLED) {
                cancelled++;
            } else if (q.getStatus() == QueueStatus.SKIPPED) {
                skipped++;
            }

            if (q.getJoinTime() != null) {
                int hour = q.getJoinTime().getHour();
                if (hour >= 0 && hour < 24) {
                    hourly[hour]++;
                }
            }
        }

        dto.setSeatedCount(seated);
        dto.setWaitingCount(waiting);
        dto.setCalledCount(called);
        dto.setCancelledCount(cancelled);
        dto.setSkippedCount(skipped);
        dto.setTotalGuestsServed(totalGuests);
        dto.setHourlyVisits(hourly);

        // Calculate peak hour
        int maxHour = -1;
        int maxCount = 0;
        for (int h = 0; h < 24; h++) {
            if (hourly[h] > maxCount) {
                maxCount = hourly[h];
                maxHour = h;
            }
        }
        if (maxHour >= 0 && maxCount > 0) {
            String amPmStart = maxHour >= 12 ? "PM" : "AM";
            int h12Start = maxHour % 12 == 0 ? 12 : maxHour % 12;
            int nextH = (maxHour + 1) % 24;
            String amPmEnd = nextH >= 12 ? "PM" : "AM";
            int h12End = nextH % 12 == 0 ? 12 : nextH % 12;
            dto.setPeakHourLabel(h12Start + ":00 " + amPmStart + " – " + h12End + ":00 " + amPmEnd);
            dto.setPeakHourCount(maxCount);
        } else {
            dto.setPeakHourLabel("No Traffic");
            dto.setPeakHourCount(0);
        }

        // Wait time stats
        if (!waitMinutesList.isEmpty()) {
            double avg = waitMinutesList.stream().mapToLong(Long::longValue).average().orElse(0.0);
            dto.setAvgWaitMinutes(Math.round(avg * 10.0) / 10.0);

            Collections.sort(waitMinutesList);
            double median;
            int size = waitMinutesList.size();
            if (size % 2 == 0) {
                median = (waitMinutesList.get(size / 2 - 1) + waitMinutesList.get(size / 2)) / 2.0;
            } else {
                median = waitMinutesList.get(size / 2);
            }
            dto.setMedianWaitMinutes(median);
        } else {
            dto.setAvgWaitMinutes(0.0);
            dto.setMedianWaitMinutes(0.0);
        }

        // Fetch reservations in range
        List<Reservation> reservations = reservationRepository.findByRestaurantIdAndReservationTimeBetweenOrderByReservationTimeAsc(
                restaurantId, startDateTime, endDateTime);

        long resTotal = reservations.size();
        long resPending = 0;
        long resConfirmed = 0;
        long resSeated = 0;
        long resCancelled = 0;
        long resNoShow = 0;

        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.PENDING) resPending++;
            else if (r.getStatus() == ReservationStatus.CONFIRMED) resConfirmed++;
            else if (r.getStatus() == ReservationStatus.SEATED) resSeated++;
            else if (r.getStatus() == ReservationStatus.CANCELLED) resCancelled++;
            else if (r.getStatus() == ReservationStatus.NO_SHOW) resNoShow++;
        }

        dto.setTotalReservations(resTotal);
        dto.setPendingReservations(resPending);
        dto.setConfirmedReservations(resConfirmed);
        dto.setSeatedReservations(resSeated);
        dto.setCancelledReservations(resCancelled);
        dto.setNoShowReservations(resNoShow);

        if (resTotal > 0) {
            double rate = ((double) resSeated / resTotal) * 100.0;
            dto.setReservationFulfillmentRate(Math.round(rate * 10.0) / 10.0);
        } else {
            dto.setReservationFulfillmentRate(0.0);
        }

        // Table capacity
        dto.setAvailableTables(tableRepository.countByRestaurantIdAndStatus(restaurantId, TableStatus.AVAILABLE));
        dto.setOccupiedTables(tableRepository.countByRestaurantIdAndStatus(restaurantId, TableStatus.OCCUPIED));

        return dto;
    }
}
