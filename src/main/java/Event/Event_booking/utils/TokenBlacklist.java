package Event.Event_booking.utils;

import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class TokenBlacklist {
    private  final Set<String> blacklistedTokens = new HashSet<>();

    //called during logout to invalidate the token
    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }
    //called on every request - checks if the token has been invalidated
    public  boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
