package com.restaurant.queue.modules.queue_management.service;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.InvalidStatusTransitionException;
import com.restaurant.queue.common.exception.QueueEntryNotFoundException;
import com.restaurant.queue.common.exception.TableNotAvailableException;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.queue_management.model.QueueHistory;
import com.restaurant.queue.modules.queue_management.repository.QueueHistoryRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for staff queue management operations:
 * - View active queue
 * - Call next customer
 * - Seat customer (with optional table assignment)
 * - Skip customer
 */
@Service
public class QueueManagementService {

    private final QueueEntryRepository queueEntryRepository;
    private final QueueHistoryRepository queueHistoryRepository;
    private final RestaurantTableRepository tableRepository;

    public QueueManagementService(QueueEntryRepository queueEntryRepository,
                                   QueueHistoryRepository queueHistoryRepository,
                                   RestaurantTableRepository tableRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.queueHistoryRepository = queueHistoryRepository;
        this.tableRepository = tableRepository;
    }

    /** Returns all WAITING and CALLED entries sorted by join time for a restaurant. */
    public List<QueueEntry> getActiveQueue(Long restaurantId) {
        if (restaurantId == null) {
            return getActiveQueue();
        }
        return queueEntryRepository.findActiveQueueByRestaurantId(restaurantId);
    }

    /** Returns all WAITING and CALLED entries sorted by join time. */
    public List<QueueEntry> getActiveQueue() {
        return queueEntryRepository.findActiveQueue();
    }

    /** Returns all queue entries for complete history view. */
    public List<QueueEntry> getAllEntries() {
        return queueEntryRepository.findAll();
    }

    /**
     * Calls the next WAITING customer for a specific restaurant: WAITING → CALLED.
     */
    @Transactional
    public Optional<QueueEntry> callNext(Long restaurantId) {
        if (restaurantId == null) {
            return callNext();
        }
        List<QueueEntry> waiting = queueEntryRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, QueueStatus.WAITING);
        if (waiting.isEmpty()) {
            return Optional.empty();
        }

        QueueEntry next = waiting.get(0);
        next.setStatus(QueueStatus.CALLED);
        next.setCalledTime(LocalDateTime.now());
        queueEntryRepository.save(next);

        logHistory(next.getId(), "CALLED");
        return Optional.of(next);
    }

    /**
     * Calls the next WAITING customer: WAITING → CALLED.
     */
    @Transactional
    public Optional<QueueEntry> callNext() {
        List<QueueEntry> waiting = queueEntryRepository.findByStatusOrderByJoinTimeAsc(QueueStatus.WAITING);
        if (waiting.isEmpty()) {
            return Optional.empty();
        }

        QueueEntry next = waiting.get(0);
        next.setStatus(QueueStatus.CALLED);
        next.setCalledTime(LocalDateTime.now());
        queueEntryRepository.save(next);

        logHistory(next.getId(), "CALLED");
        return Optional.of(next);
    }

    /**
     * Seats a customer for a specific restaurant: CALLED → SEATED. Optionally assigns a table.
     */
    @Transactional
    public void seatCustomer(Long restaurantId, Long entryId, Long tableId) {
        QueueEntry entry = (restaurantId != null)
                ? queueEntryRepository.findByIdAndRestaurantId(entryId, restaurantId)
                        .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found with ID: " + entryId))
                : queueEntryRepository.findById(entryId)
                        .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found with ID: " + entryId));

        if (entry.getStatus() != QueueStatus.CALLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot seat customer with status: " + entry.getStatus() +
                    ". Customer must be CALLED first.");
        }

        // Assign table if provided
        if (tableId != null) {
            RestaurantTable table = (restaurantId != null)
                    ? tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                            .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId))
                    : tableRepository.findById(tableId)
                            .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId));

            if (table.getStatus() != TableStatus.AVAILABLE) {
                throw new TableNotAvailableException("Table " + table.getTableNumber() + " is already occupied.");
            }

            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        }

        entry.setStatus(QueueStatus.SEATED);
        entry.setSeatedTime(LocalDateTime.now());
        queueEntryRepository.save(entry);

        logHistory(entry.getId(), "SEATED");
    }

    @Transactional
    public void seatCustomer(Long entryId, Long tableId) {
        seatCustomer(null, entryId, tableId);
    }

    /**
     * Skips a customer: WAITING or CALLED → SKIPPED.
     */
    @Transactional
    public void skipCustomer(Long restaurantId, Long entryId) {
        QueueEntry entry = (restaurantId != null)
                ? queueEntryRepository.findByIdAndRestaurantId(entryId, restaurantId)
                        .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found with ID: " + entryId))
                : queueEntryRepository.findById(entryId)
                        .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found with ID: " + entryId));

        if (entry.getStatus() != QueueStatus.WAITING && entry.getStatus() != QueueStatus.CALLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot skip a customer with status: " + entry.getStatus());
        }

        entry.setStatus(QueueStatus.SKIPPED);
        queueEntryRepository.save(entry);

        logHistory(entry.getId(), "SKIPPED");
    }

    @Transactional
    public void skipCustomer(Long entryId) {
        skipCustomer(null, entryId);
    }

    // ============================================================
    // Private Helpers
    // ============================================================

    private void logHistory(Long queueEntryId, String action) {
        QueueHistory history = new QueueHistory();
        history.setQueueEntryId(queueEntryId);
        history.setAction(action);
        history.setTimestamp(LocalDateTime.now());
        queueHistoryRepository.save(history);
    }
}
