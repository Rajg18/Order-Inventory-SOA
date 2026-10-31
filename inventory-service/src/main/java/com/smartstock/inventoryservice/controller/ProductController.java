package com.smartstock.inventoryservice.controller;

import com.smartstock.inventoryservice.dto.CreateProductRequest;
import com.smartstock.inventoryservice.dto.StockAvailabilityResponse;
import com.smartstock.inventoryservice.dto.UpdateProductRequest;
import com.smartstock.inventoryservice.dto.UpdateStockRequest;
import com.smartstock.inventoryservice.model.Product;
import com.smartstock.inventoryservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                                 @Valid @RequestBody UpdateProductRequest request) {
        Product updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, 
                                               @Valid @RequestBody UpdateStockRequest request) {
        Product updated = productService.updateStock(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<StockAvailabilityResponse> checkAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(productService.checkAvailability(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product " + id + " deleted successfully.");
        return ResponseEntity.ok(response);
    }
}
