package com.ubb.events;

import com.ubb.events.dto.RegistrationResponse;
import com.ubb.events.dto.RegistrationDetailsResponse;
import java.util.List;
import com.ubb.events.dto.EventRegistrationResponse;
public interface RegistrationService {
    RegistrationResponse registerForEvent(Long eventId, Long userId);
    void cancelRegistration(Long eventId, Long userId);
    List<RegistrationDetailsResponse> listMyRegistrations(Long userId);
    List<EventRegistrationResponse> listEventRegistrations(Long organizationId, Long eventId);
}
