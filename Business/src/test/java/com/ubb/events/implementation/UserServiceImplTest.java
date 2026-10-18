package com.ubb.events.implementation;

import com.ubb.events.domain.User;
import com.ubb.events.persistence.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserServiceImpl service = new UserServiceImpl(repository);

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
}
