package com.ubb.events.common.configs;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderConfigTest {
    @Test
    void exposesBcryptEncoder() {
        PasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
        assertTrue(encoder.matches("Str0ng!Pass", encoder.encode("Str0ng!Pass")));
        assertFalse(encoder.matches("Wrong!Pass", encoder.encode("Str0ng!Pass")));
    }
}
