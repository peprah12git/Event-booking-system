package Event.Event_booking.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed token store. Active when {@code redis.enabled=true} (default).
 * Set {@code redis.enabled=false} in application.properties to use the in-memory fallback.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RefreshTokenStoreService implements TokenStore {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenStoreService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveRefreshToken(String email, String refreshToken, long ttlMillis) {
        String key = buildKey(email);
        stringRedisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(ttlMillis));
    }

    public boolean isRefreshTokenMatch(String email, String refreshToken) {
        String storedToken = stringRedisTemplate.opsForValue().get(buildKey(email));
        return refreshToken != null && refreshToken.equals(storedToken);
    }

    public void deleteRefreshToken(String email) {
        stringRedisTemplate.delete(buildKey(email));
    }

    private String buildKey(String email) {
        return REFRESH_TOKEN_KEY_PREFIX + email;
    }
}

