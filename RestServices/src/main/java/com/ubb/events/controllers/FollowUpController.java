package com.ubb.events.controllers;

import com.ubb.events.FollowUpService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/followups")
public class FollowUpController {
    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }
}
