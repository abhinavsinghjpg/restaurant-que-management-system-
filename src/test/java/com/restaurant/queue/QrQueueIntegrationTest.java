package com.restaurant.queue;

import com.restaurant.queue.modules.restaurant_management.model.Restaurant;
import com.restaurant.queue.modules.restaurant_management.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QrQueueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = restaurantRepository.findBySlug("default-restaurant").orElseGet(() -> {
            Restaurant r = new Restaurant("default-restaurant", "Default Bistro");
            r.setActive(true);
            return restaurantRepository.save(r);
        });
    }

    @Test
    void testShowRestaurantQrPageSuccess() throws Exception {
        mockMvc.perform(get("/r/" + testRestaurant.getSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/qr_join"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("waitingCount"))
                .andExpect(model().attributeExists("qrBase64"))
                .andExpect(content().string(containsString(testRestaurant.getName())));
    }

    @Test
    void testShowRestaurantQrPageNotFound() throws Exception {
        mockMvc.perform(get("/r/non-existent-slug-xyz"))
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("errorTitle", "Not Found"));
    }

    @Test
    void testStreamQrCodePng() throws Exception {
        mockMvc.perform(get("/r/" + testRestaurant.getSlug() + "/qr.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().string("Content-Disposition", containsString("inline; filename=\"qr-" + testRestaurant.getSlug() + ".png\"")))
                .andExpect(result -> {
                    byte[] bytes = result.getResponse().getContentAsByteArray();
                    org.junit.jupiter.api.Assertions.assertTrue(bytes.length > 100, "PNG image bytes must be non-empty");
                });
    }

    @Test
    void testPrintRestaurantQrStandee() throws Exception {
        mockMvc.perform(get("/r/" + testRestaurant.getSlug() + "/print"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/qr_print"))
                .andExpect(model().attributeExists("qrBase64"))
                .andExpect(content().string(containsString(testRestaurant.getName())));
    }

    @Test
    void testJoinQueueViaQrPage() throws Exception {
        mockMvc.perform(post("/r/" + testRestaurant.getSlug() + "/join")
                        .with(csrf())
                        .param("customerName", "QR Customer Test")
                        .param("mobile", "9123456780")
                        .param("partySize", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/queue/*"));
    }
}
