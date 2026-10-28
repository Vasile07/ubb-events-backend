package com.ubb.events;

import com.ubb.events.dto.RegistrationResponse;
import com.ubb.events.dto.RegistrationDetailsResponse;
import java.util.List;
public interface RegistrationService {
    RegistrationResponse registerForEvent(Long eventId, Long userId);
    void cancelRegistration(Long eventId, Long userId);
    List<RegistrationDetailsResponse> listMyRegistrations(Long userId);
}
