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
     * Generates the next token for today's queue.
     * Format: A-01, A-02, ..., A-99
     */
    public String generateToken() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long countToday = queueEntryRepository.countTodaysEntries(startOfDay);
        int nextNumber = (int) (countToday + 1);
        return String.format("A-%02d", nextNumber);
    }
}
