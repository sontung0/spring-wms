package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.domain.UserNotFoundException;
import nst.wms.user.internal.infrastructure.UserRepository;
import nst.wms.user.internal.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
}
