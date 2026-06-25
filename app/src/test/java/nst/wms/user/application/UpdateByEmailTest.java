package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateByEmailTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateByEmail_shouldCreateNewUserWhenEmailNotFound() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User result = userService.updateByEmail("new@example.com", "New User", "https://avatar.url/img.png");

        assertEquals(1L, result.getId());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New User", result.getName());
        assertEquals("https://avatar.url/img.png", result.getAvatarUrl());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateByEmail_shouldUpdateExistingUser() {
        User existing = new User(1L, "Old Name", "existing@example.com", null, LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateByEmail("existing@example.com", "New Name", null);

        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("existing@example.com", result.getEmail());
        assertNull(result.getAvatarUrl());
    }

    @Test
    void updateByEmail_shouldOnlyUpdateNonNullFields() {
        User existing = new User(1L, "Old Name", "user@example.com", "old-avatar", LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateByEmail("user@example.com", null, null);

        assertEquals("Old Name", result.getName());
        assertEquals("old-avatar", result.getAvatarUrl());
    }
}
