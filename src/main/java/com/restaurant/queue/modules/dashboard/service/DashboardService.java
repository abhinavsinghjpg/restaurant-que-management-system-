package com.restaurant.queue.modules.dashboard.service;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.dashboard.dto.DashboardStatsResponse;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregates daily statistics for the admin dashboard.
 */
@Service
public class DashboardService {

    private final QueueEntryRepository queueEntryRepository;
    private final RestaurantTableRepository tableRepository;

    public DashboardService(QueueEntryRepository queueEntryRepository,
                             RestaurantTableRepository tableRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.tableRepository = tableRepository;
    }

    public DashboardStatsResponse getStats(Long restaurantId) {
        if (restaurantId == null) {
            return getStats();
        }
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setCustomersServedToday(
                queueEntryRepository.countByRestaurantIdAndStatusAndDate(restaurantId, QueueStatus.SEATED, startOfDay, endOfDay));

        stats.setCurrentlyWaiting(queueEntryRepository.countByRestaurantIdAndStatus(restaurantId, QueueStatus.WAITING));
        stats.setCurrentlyCalled(queueEntryRepository.countByRestaurantIdAndStatus(restaurantId, QueueStatus.CALLED));

        stats.setSkippedToday(
                queueEntryRepository.countByRestaurantIdAndStatusAndDate(restaurantId, QueueStatus.SKIPPED, startOfDay, endOfDay));
        stats.setCancelledToday(
                queueEntryRepository.countByRestaurantIdAndStatusAndDate(restaurantId, QueueStatus.CANCELLED, startOfDay, endOfDay));

        List<QueueEntry> seatedToday = queueEntryRepository.findSeatedTodayFromByRestaurant(restaurantId, startOfDay);
        long avgWait = calculateAvgWaitMinutes(seatedToday);
        stats.setAvgWaitingTimeMinutes(avgWait);

        stats.setAvailableTables(tableRepository.countByRestaurantIdAndStatus(restaurantId, TableStatus.AVAILABLE));
        stats.setOccupiedTables(tableRepository.countByRestaurantIdAndStatus(restaurantId, TableStatus.OCCUPIED));

        return stats;
    }

    public DashboardStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Today's served count
        stats.setCustomersServedToday(
                queueEntryRepository.countByStatusAndDate(QueueStatus.SEATED, startOfDay, endOfDay));

        // Current live counts
        stats.setCurrentlyWaiting(queueEntryRepository.countByStatus(QueueStatus.WAITING));
        stats.setCurrentlyCalled(queueEntryRepository.countByStatus(QueueStatus.CALLED));

        // Today's skipped/cancelled
        stats.setSkippedToday(
                queueEntryRepository.countByStatusAndDate(QueueStatus.SKIPPED, startOfDay, endOfDay));
        stats.setCancelledToday(
                queueEntryRepository.countByStatusAndDate(QueueStatus.CANCELLED, startOfDay, endOfDay));

        // Average wait time for seated customers today
        List<QueueEntry> seatedToday = queueEntryRepository.findSeatedTodayFrom(startOfDay);
        long avgWait = calculateAvgWaitMinutes(seatedToday);
        stats.setAvgWaitingTimeMinutes(avgWait);

        // Table stats
        stats.setAvailableTables(tableRepository.countByStatus(TableStatus.AVAILABLE));
        stats.setOccupiedTables(tableRepository.countByStatus(TableStatus.OCCUPIED));

        return stats;
    }

    private long calculateAvgWaitMinutes(List<QueueEntry> entries) {
        if (entries.isEmpty()) return 0;

        long totalMinutes = entries.stream()
                .filter(e -> e.getCalledTime() != null)
                .mapToLong(e -> Duration.between(e.getJoinTime(), e.getCalledTime()).toMinutes())
                .sum();

        long count = entries.stream()
                .filter(e -> e.getCalledTime() != null)
                .count();

        return count > 0 ? totalMinutes / count : 0;
    }
}
