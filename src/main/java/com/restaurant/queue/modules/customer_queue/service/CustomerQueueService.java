package com.restaurant.queue.modules.customer_queue.service;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.exception.InvalidStatusTransitionException;
import com.restaurant.queue.common.exception.QueueEntryNotFoundException;
import com.restaurant.queue.common.util.TokenGenerator;
import com.restaurant.queue.config.AppConfig;
import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.dto.QueueStatusResponse;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.queue_management.model.QueueHistory;
import com.restaurant.queue.modules.queue_management.repository.QueueHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for all customer-facing queue operations:
 * - Joining the queue
 * - Viewing queue status (position, EWT)
 * - Cancelling a queue entry
 */
@Service
public class CustomerQueueService {

    private final QueueEntryRepository queueEntryRepository;
    private final QueueHistoryRepository queueHistoryRepository;
    private final TokenGenerator tokenGenerator;
    private final AppConfig appConfig;
    private final com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository;

    public CustomerQueueService(QueueEntryRepository queueEntryRepository,
                                QueueHistoryRepository queueHistoryRepository,
                                TokenGenerator tokenGenerator,
                                AppConfig appConfig,
                                com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository restaurantRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.queueHistoryRepository = queueHistoryRepository;
        this.tokenGenerator = tokenGenerator;
        this.appConfig = appConfig;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Joins the queue for a specific restaurant: validates input, creates entry, generates token, logs history.
     */
    @Transactional
    public QueueEntry joinQueue(Long restaurantId, JoinQueueRequest request) {
        Long targetRestaurantId = restaurantId != null ? restaurantId : 1L;

        QueueEntry entry = new QueueEntry();
        entry.setRestaurantId(targetRestaurantId);
        entry.setCustomerName(request.getCustomerName().trim());
        entry.setMobile(request.getMobile().trim());
        entry.setPartySize(request.getPartySize());
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinTime(LocalDateTime.now());
        entry.setTokenNumber(tokenGenerator.generateToken(targetRestaurantId));

        QueueEntry saved = queueEntryRepository.save(entry);

        // Log history
        logHistory(saved.getId(), "JOINED");

        return saved;
    }

    @Transactional
    public QueueEntry joinQueue(JoinQueueRequest request) {
        return joinQueue(1L, request);
    }

    /**
     * Returns the full status info for a customer's token.
     */
    public QueueStatusResponse getQueueStatus(String token) {
        QueueEntry entry = queueEntryRepository.findByTokenNumber(token)
                .orElseThrow(() -> new QueueEntryNotFoundException(
                        "No queue entry found for token: " + token));

        QueueStatusResponse response = new QueueStatusResponse();
        response.setTokenNumber(entry.getTokenNumber());
        response.setCustomerName(entry.getCustomerName());
        response.setPartySize(entry.getPartySize());
        response.setStatus(entry.getStatus());
        response.setJoinTime(entry.getJoinTime());

        Long rId = entry.getRestaurantId() != null ? entry.getRestaurantId() : 1L;
        restaurantRepository.findById(rId).ifPresent(r -> {
            response.setRestaurantName(r.getName());
            response.setRestaurantSlug(r.getSlug());
        });

        if (entry.getStatus() == QueueStatus.WAITING) {
            long position = queueEntryRepository.countWaitingBeforeByRestaurant(rId, entry.getJoinTime());
            response.setPosition(position);
            response.setEstimatedWaitMins((int) (position * appConfig.getAvgServiceTimeMinutes()));
        } else {
            response.setPosition(0);
            response.setEstimatedWaitMins(0);
        }

        return response;
    }

    /**
     * Cancels a customer's queue entry. Only WAITING entries can be cancelled.
     */
    @Transactional
    public void cancelQueue(String token) {
        QueueEntry entry = queueEntryRepository.findByTokenNumber(token)
                .orElseThrow(() -> new QueueEntryNotFoundException(
                        "No queue entry found for token: " + token));

        if (entry.getStatus() != QueueStatus.WAITING) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel a queue entry with status: " + entry.getStatus() +
                    ". Only WAITING entries can be cancelled.");
        }

        entry.setStatus(QueueStatus.CANCELLED);
        queueEntryRepository.save(entry);

        logHistory(entry.getId(), "CANCELLED");
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
