package com.smartstock.orderservice.repository;

import com.smartstock.orderservice.model.ServiceEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceEventLogRepository extends JpaRepository<ServiceEventLog, Long> {
    List<ServiceEventLog> findTop50ByOrderByTimestampDesc();
}
