package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.dto.EventDiscoveryFilter;
import com.ubb.events.dto.EventSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class DiscoverEventController {
    private final EventService eventService;

    public DiscoverEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Discover, search and filter published events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching published events in chronological order; empty list when none"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value or date range"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid")
    })
    public ResponseEntity<List<EventSummaryResponse>> discoverEvents(
            @Parameter(description = "Matches the event title or the organization name, case-insensitive")
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "organizationId", required = false) Long organizationId,
            @Parameter(description = "Earliest event date (inclusive), YYYY-MM-DD")
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Latest event date (inclusive), YYYY-MM-DD")
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(eventService.discoverEvents(new EventDiscoveryFilter(search, organizationId, from, to)));
    }
}
