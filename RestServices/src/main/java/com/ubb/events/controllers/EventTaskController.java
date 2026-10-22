package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-tasks")
public class EventTaskController {
    private final EventTaskService eventTaskService;

    public EventTaskController(EventTaskService eventTaskService) {
        this.eventTaskService = eventTaskService;
    }
}
