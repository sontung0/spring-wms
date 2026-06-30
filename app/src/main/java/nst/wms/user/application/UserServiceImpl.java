package nst.wms.user.application;

import lombok.RequiredArgsConstructor;
import nst.wms.common.error.NotFoundException;
import nst.wms.user.domain.User;
import nst.wms.user.domain.UserFilter;
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import nst.wms.user.infrastructure.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public User create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(saved));
        return saved;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public Page<User> search(UserFilter filter, Pageable pageable) {
        return userRepository.search(filter, pageable);
    }

    @Override
    public User update(Long id, User user) {
        User existing = findById(id);
        existing.setName(user.getName());
        existing.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateByEmail(String email, UserUpdateData data) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    if (data.name != null) {
                        existing.setName(data.name);
                    }
                    if (data.avatarUrl != null) {
                        existing.setAvatarUrl(data.avatarUrl);
                    }
                    existing.setUpdatedAt(LocalDateTime.now());
                    User saved = userRepository.save(existing);
                    eventPublisher.publishEvent(new UserUpdatedEvent(saved));
                    return saved;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(data.name);
                    newUser.setAvatarUrl(data.avatarUrl);
                    LocalDateTime now = LocalDateTime.now();
                    newUser.setCreatedAt(now);
                    newUser.setUpdatedAt(now);
                    User saved = userRepository.save(newUser);
                    eventPublisher.publishEvent(new UserCreatedEvent(saved));
                    return saved;
                });
    }
}
