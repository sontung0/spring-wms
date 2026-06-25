package nst.wms.auth.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CaffeineStateCache implements StateCache {

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public void put(String state, String provider) {
        cache.put(state, provider);
    }

    @Override
    public String getAndEvict(String state) {
        String provider = cache.getIfPresent(state);
        cache.invalidate(state);
        return provider;
    }
}
