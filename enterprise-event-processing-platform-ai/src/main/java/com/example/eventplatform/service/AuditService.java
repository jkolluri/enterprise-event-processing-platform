package com.example.eventplatform.service;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAudit;
import com.example.eventplatform.repository.EventAuditRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final EventAuditRepository repository;
    public AuditService(EventAuditRepository repository) { this.repository = repository; }

    public void record(BusinessEvent event, String message) {
        EventAudit audit = new EventAudit();
        audit.setEventId(event.getId());
        audit.setStatus(event.getStatus());
        audit.setMessage(message);
        repository.save(audit);
    }
}
