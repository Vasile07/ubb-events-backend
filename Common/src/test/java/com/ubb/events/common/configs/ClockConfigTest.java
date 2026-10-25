package com.ubb.events.common.configs;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ClockConfigTest {
    @Test
    void exposesClockInClujLocalTime() {
        assertEquals(ZoneId.of("Europe/Bucharest"), new ClockConfig().clock().getZone());
    }
}
