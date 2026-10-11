package com.restaurant.queue;

import com.restaurant.queue.common.constants.QueueStatus;
import com.restaurant.queue.modules.customer_queue.dto.JoinQueueRequest;
import com.restaurant.queue.modules.customer_queue.model.QueueEntry;
import com.restaurant.queue.modules.customer_queue.service.CustomerQueueService;
import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RealtimeQueueApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerQueueService customerQueueService;

    private Restaurant restaurant;
    private QueueEntry entry;

    @BeforeEach
    void setUp() {
        restaurant = restaurantRepository.findBySlug("default-restaurant").orElseGet(() -> {
            Restaurant r = new Restaurant("Main Cafe", "default-restaurant");
            r.setActive(true);
            return restaurantRepository.save(r);
        });

        JoinQueueRequest req = new JoinQueueRequest();
        req.setCustomerName("Live Poll User");
        req.setMobile("9888877777");
        req.setPartySize(2);
        entry = customerQueueService.joinQueue(restaurant.getId(), req);
    }

    @Test
    void testGetCustomerTokenStatusPublic() throws Exception {
        mockMvc.perform(get("/api/queue/status/" + entry.getTokenNumber()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenNumber", is(entry.getTokenNumber())))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.customerName", is("Live Poll User")))
                .andExpect(jsonPath("$.partySize", is(2)))
                .andExpect(jsonPath("$.restaurantName", notNullValue()));
    }

    @Test
    void testGetRestaurantSummaryPublic() throws Exception {
        mockMvc.perform(get("/api/r/" + restaurant.getSlug() + "/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.restaurantName", is(restaurant.getName())))
                .andExpect(jsonPath("$.restaurantSlug", is(restaurant.getSlug())))
                .andExpect(jsonPath("$.waitingCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeEntries", notNullValue()));
    }

    @Test
    void testGetStaffQueueUnauthenticatedForbiddenOrRedirect() throws Exception {
        mockMvc.perform(get("/api/admin/queue/data"))
                .andExpect(status().is3xxRedirection()); // Redirects to login
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetStaffQueueAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/queue/data").param("restaurantId", String.valueOf(restaurant.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.restaurantId", is(restaurant.getId().intValue())))
                .andExpect(jsonPath("$.entries", notNullValue()))
                .andExpect(jsonPath("$.availableTables", notNullValue()));
    }
}
