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

    public CustomerQueueService(QueueEntryRepository queueEntryRepository,
                                QueueHistoryRepository queueHistoryRepository,
                                TokenGenerator tokenGenerator,
                                AppConfig appConfig) {
        this.queueEntryRepository = queueEntryRepository;
        this.queueHistoryRepository = queueHistoryRepository;
        this.tokenGenerator = tokenGenerator;
        this.appConfig = appConfig;
    }

    /**
     * Joins the queue: validates input, creates entry, generates token, logs history.
     */
    @Transactional
    public QueueEntry joinQueue(JoinQueueRequest request) {
        QueueEntry entry = new QueueEntry();
        entry.setCustomerName(request.getCustomerName().trim());
        entry.setMobile(request.getMobile().trim());
        entry.setPartySize(request.getPartySize());
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinTime(LocalDateTime.now());
        entry.setTokenNumber(tokenGenerator.generateToken());

        QueueEntry saved = queueEntryRepository.save(entry);

        // Log history
        logHistory(saved.getId(), "JOINED");

        return saved;
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

        if (entry.getStatus() == QueueStatus.WAITING) {
            long position = queueEntryRepository.countWaitingBefore(entry.getJoinTime());
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
