package Event.Event_booking.utils;

import java.util.HashSet;
import java.util.Set;

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
