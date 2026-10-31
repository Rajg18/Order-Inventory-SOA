package com.smartstock.inventoryservice.controller;

import com.smartstock.inventoryservice.model.ServiceEventLog;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/events")
@CrossOrigin(origins = "*")
public class EventLogController {

    private final ServiceEventLogRepository eventLogRepository;

    public EventLogController(ServiceEventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<ServiceEventLog>> getRecentEvents() {
        return ResponseEntity.ok(eventLogRepository.findTop50ByOrderByTimestampDesc());
    }
}
