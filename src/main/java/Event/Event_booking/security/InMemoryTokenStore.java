package Event.Event_booking.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fallback token store. Active when {@code redis.enabled=false}.
 * Refresh tokens are stored in a ConcurrentHashMap with manual TTL tracking.
 * Use this for local development without a running Redis instance.
 * NOTE: tokens are lost on application restart and not shared across instances.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
public class InMemoryTokenStore implements TokenStore {

    private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiries = new ConcurrentHashMap<>();

    @Override
    public void saveRefreshToken(String email, String refreshToken, long ttlMillis) {
        tokens.put(email, refreshToken);
        expiries.put(email, System.currentTimeMillis() + ttlMillis);
    }

    @Override
    public boolean isRefreshTokenMatch(String email, String refreshToken) {
        Long expiry = expiries.get(email);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            // Expired – clean up lazily
            tokens.remove(email);
            expiries.remove(email);
            return false;
        }
        String stored = tokens.get(email);
        return refreshToken != null && refreshToken.equals(stored);
    }

    @Override
    public void deleteRefreshToken(String email) {
        tokens.remove(email);
        expiries.remove(email);
    }
}

