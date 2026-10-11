package com.restaurant.queue;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.QueueEntryNotFoundException;
import com.restaurant.queue.common.util.TokenGenerator;
import com.restaurant.queue.modules.authentication.model.User;
import com.restaurant.queue.modules.authentication.repository.UserRepository;
import com.restaurant.queue.modules.authentication.service.AuthService;
import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.repository.QueueEntryRepository;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import com.restaurant.queue.modules.queue_management.service.QueueManagementService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import com.restaurant.queue.modules.table_management.service.TableManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiRestaurantIsolationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private CustomerQueueService customerQueueService;

    @Autowired
    private QueueManagementService queueManagementService;

    @Autowired
    private TableManagementService tableManagementService;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private Restaurant restaurant1;
    private Restaurant restaurant2;

    @BeforeEach
    void setup() {
        restaurant1 = restaurantRepository.findById(1L).orElseGet(() -> {
            Restaurant r = new Restaurant("default-restaurant", "Main Branch");
            return restaurantRepository.save(r);
        });

        restaurant2 = restaurantRepository.findBySlug("test-branch-2").orElseGet(() -> {
            Restaurant r = new Restaurant("test-branch-2", "Branch Two");
            return restaurantRepository.save(r);
        });
    }

    @Test
    void testQueueIsolationBetweenRestaurants() {
        JoinQueueRequest req1 = new JoinQueueRequest();
        req1.setCustomerName("Rest1 Customer");
        req1.setMobile("9876543211");
        req1.setPartySize(2);
        QueueEntry entry1 = customerQueueService.joinQueue(restaurant1.getId(), req1);

        JoinQueueRequest req2 = new JoinQueueRequest();
        req2.setCustomerName("Rest2 Customer");
        req2.setMobile("9876543212");
        req2.setPartySize(4);
        QueueEntry entry2 = customerQueueService.joinQueue(restaurant2.getId(), req2);

        // Verify active queue for restaurant 1 only includes entry1
        List<QueueEntry> queue1 = queueManagementService.getActiveQueue(restaurant1.getId());
        assertTrue(queue1.stream().anyMatch(e -> e.getId().equals(entry1.getId())));
        assertFalse(queue1.stream().anyMatch(e -> e.getId().equals(entry2.getId())));

        // Verify active queue for restaurant 2 only includes entry2
        List<QueueEntry> queue2 = queueManagementService.getActiveQueue(restaurant2.getId());
        assertTrue(queue2.stream().anyMatch(e -> e.getId().equals(entry2.getId())));
        assertFalse(queue2.stream().anyMatch(e -> e.getId().equals(entry1.getId())));
    }

    @Test
    void testTableIsolationBetweenRestaurants() {
        String tblNum = "T-R2-" + System.currentTimeMillis();
        RestaurantTable t1 = new RestaurantTable();
        t1.setTableNumber(tblNum);
        t1.setCapacity(4);
        t1.setStatus(TableStatus.AVAILABLE);
        t1.setRestaurantId(restaurant2.getId());
        tableRepository.save(t1);

        List<RestaurantTable> r1Tables = tableManagementService.getAllTables(restaurant1.getId());
        List<RestaurantTable> r2Tables = tableManagementService.getAllTables(restaurant2.getId());

        assertFalse(r1Tables.stream().anyMatch(t -> t.getTableNumber().equals(tblNum)));
        assertTrue(r2Tables.stream().anyMatch(t -> t.getTableNumber().equals(tblNum)));
    }

    @Test
    void testCrossRestaurantActionBlocked() {
        JoinQueueRequest req = new JoinQueueRequest();
        req.setCustomerName("Rest2 Customer For Seat");
        req.setMobile("9876543219");
        req.setPartySize(2);
        QueueEntry entry2 = customerQueueService.joinQueue(restaurant2.getId(), req);

        // Restaurant 1 staff attempting to seat or skip Restaurant 2's entry must fail
        assertThrows(QueueEntryNotFoundException.class, () -> {
            queueManagementService.seatCustomer(restaurant1.getId(), entry2.getId(), null);
        });

        assertThrows(QueueEntryNotFoundException.class, () -> {
            queueManagementService.skipCustomer(restaurant1.getId(), entry2.getId());
        });
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testStaffAuthorizationBoundToAssignedRestaurant() {
        Long staffRId = authService.resolveRestaurantId(null);
        assertEquals(1L, staffRId);

        // Staff requesting another restaurant must be denied
        assertThrows(AccessDeniedException.class, () -> {
            authService.resolveRestaurantId(999L);
        });
    }
}
