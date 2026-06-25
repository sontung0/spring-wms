package nst.wms.user.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);
    java.util.Optional<User> findById(Long id);
    java.util.Optional<User> findByEmail(String email);
    Page<User> search(UserFilter filter, Pageable pageable);
    void deleteById(Long id);
    boolean existsById(Long id);
}
