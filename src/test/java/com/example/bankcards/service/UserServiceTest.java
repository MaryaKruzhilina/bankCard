package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_returnsUserDetails_withPasswordAndAuthorities() {
        // given
        String username = "alice";
        String hashPassword = "{bcrypt}hash";
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getHashPassword()).thenReturn(hashPassword);
        when(user.getRoles()).thenReturn(List.of(Role.USER, Role.ADMIN));

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        // when
        UserDetails details = userService.loadUserByUsername(username);

        // then
        assertNotNull(details);
        assertEquals(username, details.getUsername());
        assertEquals(hashPassword, details.getPassword());

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // роли должны быть с префиксом ROLE_
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertEquals(2, authorities.size());

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        // given
        String username = "ghost";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        // when + then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getIdByUsername_returnsUserId() {
        // given
        String username = "bob";
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        // when
        UUID result = userService.getIdByUsername(username);

        // then
        assertEquals(userId, result);

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getIdByUsername_userNotFound_throwsUsernameNotFoundException() {
        // given
        String username = "nobody";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        // when + then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.getIdByUsername(username));

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }
}
