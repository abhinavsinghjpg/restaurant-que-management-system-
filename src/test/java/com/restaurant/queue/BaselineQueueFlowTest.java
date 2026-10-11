package com.restaurant.queue;

import com.restaurant.queue.common.util.TokenGenerator;
import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BaselineQueueFlowTest {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private CustomerQueueService customerQueueService;

    @Test
    void testTokenGenerationFormatAndUniqueness() {
        String token = tokenGenerator.generateToken();
        assertNotNull(token);
        assertTrue(token.matches("A-\\d+"));
        assertFalse(queueEntryRepository.findByTokenNumber(token).isPresent(),
                "Generated token should not already exist in database");
    }

    @Test
    void testJoinQueueSucceeds() {
        JoinQueueRequest req = new JoinQueueRequest();
        req.setCustomerName("Baseline Test User");
        req.setMobile("9876543210");
        req.setPartySize(3);

        QueueEntry entry = customerQueueService.joinQueue(req);
        assertNotNull(entry.getId());
        assertNotNull(entry.getTokenNumber());
        assertEquals("Baseline Test User", entry.getCustomerName());
        assertEquals(3, entry.getPartySize());
    }
}
