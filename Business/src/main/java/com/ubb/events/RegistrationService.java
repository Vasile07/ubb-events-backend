package com.ubb.events;

import com.ubb.events.dto.RegistrationResponse;
public interface RegistrationService {
    RegistrationResponse registerForEvent(Long eventId, Long userId);
}
