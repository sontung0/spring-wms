package nst.wms.user.infrastructure;

import nst.wms.user.presentation.dto.UserFilter;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

class UserSpecificationTest {

    private final UserSpecification userSpecification = new UserSpecification();

    @Test
    void fromFilter_withName_shouldReturnMatchingSpec() {
        UserFilter filter = new UserFilter();
        filter.setName("John");

        Specification<UserJpaEntity> spec = userSpecification.fromFilter(filter);

        assertNotNull(spec);
    }

    @Test
    void fromFilter_withNullFilters_shouldReturnEmptySpec() {
        UserFilter filter = new UserFilter();

        Specification<UserJpaEntity> spec = userSpecification.fromFilter(filter);

        assertNotNull(spec);
    }
}
