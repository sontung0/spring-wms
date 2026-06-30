package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import nst.wms.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateByEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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

        UserUpdateData data = new UserUpdateData();
        data.name = "New User";
        data.avatarUrl = "https://avatar.url/img.png";
        User result = userService.updateByEmail("new@example.com", data);

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

        UserUpdateData data = new UserUpdateData();
        data.name = "New Name";
        User result = userService.updateByEmail("existing@example.com", data);

        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("existing@example.com", result.getEmail());
        assertNull(result.getAvatarUrl());
    }

    @Test
    void updateByEmail_shouldPublishUserCreatedEventWhenNewUser() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserUpdateData data = new UserUpdateData();
        data.name = "New User";
        userService.updateByEmail("new@example.com", data);

        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void updateByEmail_shouldPublishUserUpdatedEventWhenExistingUser() {
        User existing = new User(1L, "Old Name", "existing@example.com", null, LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateData data = new UserUpdateData();
        data.name = "New Name";
        userService.updateByEmail("existing@example.com", data);

        verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
    }

    @Test
    void updateByEmail_shouldOnlyUpdateNonNullFields() {
        User existing = new User(1L, "Old Name", "user@example.com", "old-avatar", LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateData data = new UserUpdateData();
        User result = userService.updateByEmail("user@example.com", data);

        assertEquals("Old Name", result.getName());
        assertEquals("old-avatar", result.getAvatarUrl());
    }
}
