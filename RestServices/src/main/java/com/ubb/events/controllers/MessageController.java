package com.ubb.events.controllers;

import com.ubb.events.MessageService;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.security.RequireOrgPermission;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/organizations")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{organizationId}/events/{eventId}/messages")
    @RequireOrgPermission
    @Operation(summary = "View event messages")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event messages returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<MessageResponse>> list(@PathVariable Long organizationId, @PathVariable Long eventId) {
        return ResponseEntity.ok(messageService.listEventMessages(organizationId, eventId));
    }
}
