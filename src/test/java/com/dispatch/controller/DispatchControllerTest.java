package com.dispatch.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Integratively validates that when a fleet vehicle and a package are
     * registered, the dispatch controller accurately generates an optimized
     * routing assignment mapping.
     */
    @Test
    void shouldAcceptValidOrderAndIncludeItInDispatchPlan() throws Exception {
        // 1. Arrange: Explicitly provision a fleet resource so that the optimizer has allocation capability
        String vehiclePayload = """
            {
                "vehicles": [
                    {
                        "vehicleId": "VEH-TEST-01",
                        "capacity": 500.0,
                        "currentLatitude": 12.9716,
                        "currentLongitude": 77.5946,
                        "currentAddress": "Test Depot, Bangalore"
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/dispatch/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehiclePayload))
                .andExpect(status().isCreated());

        // 2. Act: Post the delivery order parameters under test
        String orderId = "ORD-VALID-bab4aaa6-3dfb-49b6-9fb6-e4bc5489aa26";
        String orderPayload = """
            {
                "orders": [
                    {
                        "orderId": "%s",
                        "latitude": 12.9716,
                        "longitude": 77.6413,
                        "address": "Indiranagar, Bangalore",
                        "packageWeight": 25.0,
                        "priority": "HIGH"
                    }
                ]
            }
            """.formatted(orderId);

        mockMvc.perform(post("/api/dispatch/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderPayload))
                .andExpect(status().isCreated());

        // 3. Assert: Execute routing compilation and assert the specific order ID maps cleanly in the payload response
        mockMvc.perform(get("/api/dispatch/plan")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(orderId)));
    }
}
