package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.dto.EventSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class DiscoverEventController {
    private final EventService eventService;

    public DiscoverEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Discover published events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published events returned in chronological order; empty list when none"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid")
    })
    public ResponseEntity<List<EventSummaryResponse>> discoverEvents() {
        return ResponseEntity.ok(eventService.discoverEvents());
    }
}
