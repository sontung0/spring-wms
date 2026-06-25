package nst.wms.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityJpaRepository extends JpaRepository<UserIdentityJpaEntity, Long> {
    Optional<UserIdentityJpaEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
