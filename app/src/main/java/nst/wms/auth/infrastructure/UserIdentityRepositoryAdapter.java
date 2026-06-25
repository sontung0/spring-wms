package nst.wms.auth.infrastructure;

import nst.wms.auth.domain.AuthUser;
import nst.wms.auth.domain.OAuthProviderCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserIdentityRepositoryAdapter implements UserIdentityRepository {

    private final UserIdentityJpaRepository jpaRepository;

    public UserIdentityRepositoryAdapter(UserIdentityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Long userId, OAuthProviderCode provider, AuthUser authUser) {
        UserIdentityJpaEntity entity = jpaRepository
                .findByProviderAndProviderUserId(provider.name(), authUser.getProviderUserId())
                .orElse(new UserIdentityJpaEntity());

        LocalDateTime now = LocalDateTime.now();
        entity.setUserId(userId);
        entity.setProvider(provider.name());
        entity.setProviderUserId(authUser.getProviderUserId());
        entity.setEmail(authUser.getEmail());
        entity.setName(authUser.getName());
        entity.setAvatarUrl(authUser.getAvatarUrl());

        if (entity.getId() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);

        jpaRepository.save(entity);
    }
}
