package com.restaurant.queue.common.util;

import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Generates unique daily queue tokens in the format A-01, A-02, ... A-99.
 * Tokens are scoped per day and reset each day.
 */
@Component
public class TokenGenerator {

    private final QueueEntryRepository queueEntryRepository;

    public TokenGenerator(QueueEntryRepository queueEntryRepository) {
        this.queueEntryRepository = queueEntryRepository;
    }

    /**
     * Generates the next unique token for the queue.
     * Starts from today's count + 1 and guarantees uniqueness.
     */
    public synchronized String generateToken() {
        return generateToken(1L);
    }

    /**
     * Generates the next unique token for a specific restaurant's queue.
     * Uses a restaurant prefix (A for R1, B for R2, etc.) and guarantees uniqueness.
     */
    public synchronized String generateToken(Long restaurantId) {
        if (restaurantId == null || restaurantId < 1) {
            restaurantId = 1L;
        }
        char prefix = (char) ('A' + (int) ((restaurantId - 1) % 26));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long countToday = queueEntryRepository.countTodaysEntriesByRestaurant(restaurantId, startOfDay);
        int nextNumber = (int) (countToday + 1);

        while (true) {
            String candidate = String.format("%c-%02d", prefix, nextNumber);
            if (queueEntryRepository.findByTokenNumber(candidate).isEmpty()) {
                return candidate;
            }
            nextNumber++;
        }
    }
}
