package com.smartstock.inventoryservice.repository;

import com.smartstock.inventoryservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("UPDATE Product p SET p.availableQuantity = p.availableQuantity - :quantity, " +
           "p.reservedQuantity = p.reservedQuantity + :quantity " +
           "WHERE p.id = :id AND p.availableQuantity >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.availableQuantity = p.availableQuantity + :quantity, " +
           "p.reservedQuantity = p.reservedQuantity - :quantity " +
           "WHERE p.id = :id AND p.reservedQuantity >= :quantity")
    int releaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.totalQuantity = p.totalQuantity + :quantity, " +
           "p.availableQuantity = p.availableQuantity + :quantity " +
           "WHERE p.id = :id")
    int addStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.totalQuantity = p.totalQuantity - :quantity, " +
           "p.availableQuantity = p.availableQuantity - :quantity " +
           "WHERE p.id = :id AND p.availableQuantity >= :quantity")
    int removeStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
