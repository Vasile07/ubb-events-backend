package com.ubb.events.implementation;

import com.ubb.events.domain.User;
import com.ubb.events.dto.UserResponse;
import com.ubb.events.persistence.UserRepository;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final MembershipRepository membershipRepository = mock(MembershipRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final UserServiceImpl service = new UserServiceImpl(repository, membershipRepository, passwordEncoder, jwtTokenService);

    @Test
    void delegatesSaveFindAndListToRepository() {
        User user = new User(null, "Ada", "Lovelace", "ada@example.com", "hash-1");
        when(repository.save(user)).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findAll()).thenReturn(List.of(user));

        assertEquals(user, service.save(user));
        assertEquals(Optional.of(user), service.findById(1L));
        assertEquals(List.of(user), service.findAll());
        verify(repository).save(user);
        verify(repository).findById(1L);
        verify(repository).findAll();
    }

    @Test
    void registersWithEncodedPasswordAndReturnsPasswordFreeResponse() {
        User saved = new User(7L, "Ada", "Lovelace", "ada@example.com", "encoded");
        when(repository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ng!Pass")).thenReturn("encoded");
        when(repository.save(any(User.class))).thenReturn(saved);

        UserResponse response = service.register("Ada", "Lovelace", "ada@example.com", "Str0ng!Pass");

        assertEquals(7L, response.getId());
        assertEquals("ada@example.com", response.getEmail());
        verify(passwordEncoder).encode("Str0ng!Pass");
        verify(repository).save(argThat(user -> "encoded".equals(user.getPassword())));
    }

    @Test
    void rejectsDuplicateEmailBeforeSaving() {
        when(repository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.register("Ada", "Lovelace", "ada@example.com", "Str0ng!Pass"));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void authenticatesValidCredentialsAndReturnsToken() {
        User user = new User(7L, "Ada", "Lovelace", "ada@example.com", "encoded");
        when(repository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Str0ng!Pass", "encoded")).thenReturn(true);
        when(jwtTokenService.issue(7L)).thenReturn("token");

        var response = service.login("ada@example.com", "Str0ng!Pass");

        assertEquals("token", response.getToken());
    }
}
