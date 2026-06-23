package nst.wms.user.infrastructure;

import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecification {

    public Specification<UserJpaEntity> fromFilter(UserFilter filter) {
        Specification<UserJpaEntity> spec = (root, query, cb) -> null;

        if (filter.getName() != null) {
            spec = spec.and(hasName(filter.getName()));
        }

        return spec;
    }

    private Specification<UserJpaEntity> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
