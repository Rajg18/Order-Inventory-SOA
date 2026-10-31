package com.smartstock.orderservice;

import com.smartstock.orderservice.client.InventoryClient;
import com.smartstock.orderservice.client.ProductClientResponse;
import com.smartstock.orderservice.client.StockAvailabilityClientResponse;
import com.smartstock.orderservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class InventoryClientContractTest {

    private MockRestServiceServer mockServer;
    private InventoryClient inventoryClient;

    @BeforeEach
    public void setup() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        inventoryClient = new InventoryClient(builder, "http://localhost:8082");
    }

    @Test
    public void testGetProductById_ContractSuccess() {
        String jsonResponse = """
            {
                "id": 1,
                "productName": "Fresh Alphonso Mangoes (1kg)",
                "price": 249.00,
                "imageUrl": "http://image.jpg",
                "totalQuantity": 25,
                "availableQuantity": 25,
                "reservedQuantity": 0
            }
            """;

        mockServer.expect(requestTo("http://localhost:8082/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ProductClientResponse response = inventoryClient.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Fresh Alphonso Mangoes (1kg)", response.getProductName());
        assertEquals(0, new BigDecimal("249.00").compareTo(response.getPrice()));
        assertEquals(25, response.getAvailableQuantity());
        mockServer.verify();
    }

    @Test
    public void testGetProductById_ContractNotFound() {
        mockServer.expect(requestTo("http://localhost:8082/api/products/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> inventoryClient.getProductById(999L));
        mockServer.verify();
    }

    @Test
    public void testCheckAvailability_ContractSuccess() {
        String jsonResponse = """
            {
                "productId": 1,
                "availableQuantity": 15,
                "inStock": true
            }
            """;

        mockServer.expect(requestTo("http://localhost:8082/api/products/1/availability"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        StockAvailabilityClientResponse response = inventoryClient.checkAvailability(1L);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(15, response.getAvailableQuantity());
        assertTrue(response.isInStock());
        mockServer.verify();
    }
}
