package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private Long eventId;
    private String title;
    private String content;
}
