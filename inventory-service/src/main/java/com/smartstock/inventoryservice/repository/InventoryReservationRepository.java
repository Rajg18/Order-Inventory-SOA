package com.smartstock.inventoryservice.repository;

import com.smartstock.inventoryservice.model.InventoryReservation;
import com.smartstock.inventoryservice.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    Optional<InventoryReservation> findByOrderId(Long orderId);
    List<InventoryReservation> findByProductId(Long productId);
    boolean existsByProductIdAndReservationStatus(Long productId, ReservationStatus reservationStatus);
}
