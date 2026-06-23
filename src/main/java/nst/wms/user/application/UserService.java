package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User create(User user);
    User findById(Long id);
    Page<User> search(UserFilter filter, Pageable pageable);
    User update(Long id, User user);
    void deleteById(Long id);
}
