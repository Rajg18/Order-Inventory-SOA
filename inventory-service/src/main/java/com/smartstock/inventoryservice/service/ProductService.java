package com.smartstock.inventoryservice.service;

import com.smartstock.inventoryservice.dto.CreateProductRequest;
import com.smartstock.inventoryservice.dto.StockAvailabilityResponse;
import com.smartstock.inventoryservice.dto.UpdateProductRequest;
import com.smartstock.inventoryservice.dto.UpdateStockRequest;
import com.smartstock.inventoryservice.exception.BusinessRuleConflictException;
import com.smartstock.inventoryservice.exception.ResourceNotFoundException;
import com.smartstock.inventoryservice.model.Product;
import com.smartstock.inventoryservice.model.ReservationStatus;
import com.smartstock.inventoryservice.model.ServiceEventLog;
import com.smartstock.inventoryservice.repository.InventoryReservationRepository;
import com.smartstock.inventoryservice.repository.ProductRepository;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ServiceEventLogRepository eventLogRepository;

    public ProductService(ProductRepository productRepository, 
                          InventoryReservationRepository reservationRepository,
                          ServiceEventLogRepository eventLogRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.eventLogRepository = eventLogRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setProductName(request.getProductName().trim());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setTotalQuantity(request.getTotalQuantity());
        product.setAvailableQuantity(request.getTotalQuantity());
        product.setReservedQuantity(0);

        Product saved = productRepository.save(product);

        eventLogRepository.save(new ServiceEventLog(
                null,
                "ProductCreated",
                "INTERNAL",
                null,
                saved.getId(),
                "Product '" + saved.getProductName() + "' created with initial stock: " + saved.getTotalQuantity()
        ));

        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProductById(id);
        product.setProductName(request.getProductName().trim());
        product.setPrice(request.getPrice());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updated = productRepository.save(product);

        eventLogRepository.save(new ServiceEventLog(
                null,
                "ProductUpdated",
                "INTERNAL",
                null,
                updated.getId(),
                "Product details updated for '" + updated.getProductName() + "', new price: $" + updated.getPrice()
        ));

        return updated;
    }

    @Transactional
    public Product updateStock(Long id, UpdateStockRequest request) {
        Product product = getProductById(id);
        String action = request.getAction().toUpperCase();
        int qty = request.getQuantity();

        if ("ADD".equals(action)) {
            productRepository.addStock(id, qty);
            eventLogRepository.save(new ServiceEventLog(
                    null,
                    "StockAdded",
                    "INTERNAL",
                    null,
                    id,
                    "Admin added " + qty + " units to product '" + product.getProductName() + "'"
            ));
        } else if ("REMOVE".equals(action)) {
            if (product.getAvailableQuantity() < qty) {
                throw new BusinessRuleConflictException(
                        "Cannot remove " + qty + " units. Available stock is only " + 
                        product.getAvailableQuantity() + " (Reserved stock: " + product.getReservedQuantity() + ")"
                );
            }
            int updatedRows = productRepository.removeStock(id, qty);
            if (updatedRows == 0) {
                throw new BusinessRuleConflictException("Stock removal failed due to concurrent modification.");
            }
            eventLogRepository.save(new ServiceEventLog(
                    null,
                    "StockRemoved",
                    "INTERNAL",
                    null,
                    id,
                    "Admin removed " + qty + " units from product '" + product.getProductName() + "'"
            ));
        } else {
            throw new IllegalArgumentException("Invalid stock action: " + action);
        }

        return getProductById(id);
    }

    @Transactional(readOnly = true)
    public StockAvailabilityResponse checkAvailability(Long id) {
        Product product = getProductById(id);
        boolean inStock = product.getAvailableQuantity() > 0;
        return new StockAvailabilityResponse(product.getId(), product.getAvailableQuantity(), inStock);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);

        if (product.getReservedQuantity() > 0 ||
                reservationRepository.existsByProductIdAndReservationStatus(id, ReservationStatus.RESERVED)) {
            throw new BusinessRuleConflictException(
                    "Cannot delete product '" + product.getProductName() + 
                    "' (ID: " + id + ") because it has active stock reservations."
            );
        }

        productRepository.delete(product);

        eventLogRepository.save(new ServiceEventLog(
                null,
                "ProductDeleted",
                "INTERNAL",
                null,
                id,
                "Product ID " + id + " ('" + product.getProductName() + "') was deleted."
        ));
    }
}
