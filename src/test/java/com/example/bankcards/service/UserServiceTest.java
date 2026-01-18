package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_returnsUserDetailsFromDb() {
        // given
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("admin");
        u.setHashPassword("$2a$12$hash");
        u.setRoles(List.of(Role.ADMIN, Role.USER));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(u));

        // when
        UserDetails details = userService.loadUserByUsername("admin");

        // then
        assertEquals("admin", details.getUsername());
        assertEquals("$2a$12$hash", details.getPassword());

        var authorities = details.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_USER"));

        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        // given
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // when / then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("ghost"));

        verify(userRepository).findByUsername("ghost");
        verifyNoMoreInteractions(userRepository);
    }
}