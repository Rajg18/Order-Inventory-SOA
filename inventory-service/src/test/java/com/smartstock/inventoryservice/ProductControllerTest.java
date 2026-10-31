package com.smartstock.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstock.inventoryservice.controller.ProductController;
import com.smartstock.inventoryservice.dto.CreateProductRequest;
import com.smartstock.inventoryservice.dto.StockAvailabilityResponse;
import com.smartstock.inventoryservice.dto.UpdateProductRequest;
import com.smartstock.inventoryservice.dto.UpdateStockRequest;
import com.smartstock.inventoryservice.exception.BusinessRuleConflictException;
import com.smartstock.inventoryservice.exception.GlobalExceptionHandler;
import com.smartstock.inventoryservice.exception.ResourceNotFoundException;
import com.smartstock.inventoryservice.model.Product;
import com.smartstock.inventoryservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetAllProducts() throws Exception {
        Product p = new Product(1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), "http://img.jpg", 25, 25, 0);
        when(productService.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Fresh Alphonso Mangoes"))
                .andExpect(jsonPath("$[0].totalQuantity").value(25))
                .andExpect(jsonPath("$[0].availableQuantity").value(25));
    }

    @Test
    public void testGetProductById_Found() throws Exception {
        Product p = new Product(1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), "http://img.jpg", 25, 25, 0);
        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Fresh Alphonso Mangoes"));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void testCreateProduct_Success() throws Exception {
        CreateProductRequest req = new CreateProductRequest("Greek Yogurt", BigDecimal.valueOf(60.00), "http://yogurt.jpg", 30);
        Product created = new Product(2L, "Greek Yogurt", BigDecimal.valueOf(60.00), "http://yogurt.jpg", 30, 30, 0);
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.productName").value("Greek Yogurt"));
    }

    @Test
    public void testUpdateStock_Success() throws Exception {
        UpdateStockRequest req = new UpdateStockRequest("ADD", 10);
        Product updated = new Product(1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), "http://img.jpg", 35, 35, 0);
        when(productService.updateStock(eq(1L), any(UpdateStockRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(35));
    }

    @Test
    public void testUpdateStock_ConflictWhenExcessiveRemove() throws Exception {
        UpdateStockRequest req = new UpdateStockRequest("REMOVE", 100);
        when(productService.updateStock(eq(1L), any(UpdateStockRequest.class)))
                .thenThrow(new BusinessRuleConflictException("Cannot remove 100 units. Available stock is only 25"));

        mockMvc.perform(put("/api/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot remove 100 units. Available stock is only 25"));
    }

    @Test
    public void testCheckAvailability() throws Exception {
        when(productService.checkAvailability(1L)).thenReturn(new StockAvailabilityResponse(1L, 25, true));

        mockMvc.perform(get("/api/products/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.availableQuantity").value(25))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    public void testDeleteProduct_ConflictWithActiveReservations() throws Exception {
        doThrow(new BusinessRuleConflictException("Cannot delete product because it has active stock reservations."))
                .when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
