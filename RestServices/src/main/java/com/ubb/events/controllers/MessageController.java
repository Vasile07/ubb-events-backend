package com.ubb.events.controllers;

import com.ubb.events.MessageService;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.dto.SendMessageRequest;
import com.ubb.events.security.RequireOrgPermission;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/{organizationId}/events/{eventId}/messages")
    @RequireOrgPermission
    @Operation(summary = "Send an event message")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message created"),
            @ApiResponse(responseCode = "400", description = "Message content is invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Event or author not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled")
    })
    public ResponseEntity<MessageResponse> send(@PathVariable Long organizationId, @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId, @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(organizationId, eventId, userId, request));
    }
}
