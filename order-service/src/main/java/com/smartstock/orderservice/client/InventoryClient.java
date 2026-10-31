package com.smartstock.orderservice.client;

import com.smartstock.orderservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(RestClient.Builder restClientBuilder, 
                           @Value("${inventory-service.url:http://localhost:8082}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public ProductClientResponse getProductById(Long productId) {
        return restClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ResourceNotFoundException("Product with ID " + productId + " does not exist in Inventory Service.");
                })
                .body(ProductClientResponse.class);
    }

    public StockAvailabilityClientResponse checkAvailability(Long productId) {
        return restClient.get()
                .uri("/api/products/{id}/availability", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ResourceNotFoundException("Product with ID " + productId + " does not exist in Inventory Service.");
                })
                .body(StockAvailabilityClientResponse.class);
    }
}
