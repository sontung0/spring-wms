package nst.wms.user.application;

import lombok.RequiredArgsConstructor;
import nst.wms.user.domain.User;
import nst.wms.user.domain.UserNotFoundException;
import nst.wms.user.infrastructure.UserRepository;
import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
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
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
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
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(data.name);
                    newUser.setAvatarUrl(data.avatarUrl);
                    LocalDateTime now = LocalDateTime.now();
                    newUser.setCreatedAt(now);
                    newUser.setUpdatedAt(now);
                    return userRepository.save(newUser);
                });
    }
}
