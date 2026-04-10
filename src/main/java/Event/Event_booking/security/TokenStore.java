package Event.Event_booking.security;

/**
 * Abstraction over the refresh-token persistence layer.
 * Allows swapping between Redis (production) and in-memory (local dev)
 * by toggling {@code redis.enabled} in application.properties.
 */
public interface TokenStore {
    void saveRefreshToken(String email, String refreshToken, long ttlMillis);
    boolean isRefreshTokenMatch(String email, String refreshToken);
    void deleteRefreshToken(String email);
}

