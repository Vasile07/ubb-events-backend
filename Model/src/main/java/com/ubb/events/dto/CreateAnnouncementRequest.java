package com.ubb.events.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementRequest {
    @NotBlank(message = "title is required")
    private String title;
    @NotBlank(message = "content is required")
    private String content;
}
