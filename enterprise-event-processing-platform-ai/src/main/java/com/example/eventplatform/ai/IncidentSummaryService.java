package com.example.eventplatform.ai;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.repository.BusinessEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class IncidentSummaryService {
    private final BusinessEventRepository eventRepository;

    public IncidentSummaryService(BusinessEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public IncidentSummary summarize(int minutes) {
        int safeMinutes = Math.max(1, Math.min(minutes, 1440));
        Instant end = Instant.now();
        Instant start = end.minus(safeMinutes, ChronoUnit.MINUTES);
        List<BusinessEvent> failures = eventRepository.findByStatusInAndUpdatedAtAfterOrderByUpdatedAtDesc(List.of(EventStatus.FAILED, EventStatus.DEAD_LETTER), start);
        List<String> types = failures.stream().map(BusinessEvent::getEventType).distinct().sorted().toList();
        String summary = failures.isEmpty()
                ? "No failed events were recorded in the selected window."
                : failures.size() + " failed event(s) were recorded across " + types.size() + " event type(s).";
        String action = failures.isEmpty()
                ? "No incident action is currently required."
                : "Review the latest AI failure analyses, group recurring categories, and approve retries only for retry-safe failures.";
        return new IncidentSummary(start, end, failures.size(), types, summary, action);
    }
}
