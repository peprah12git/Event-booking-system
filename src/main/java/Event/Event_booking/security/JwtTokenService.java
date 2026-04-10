package Event.Event_booking.security;

import Event.Event_booking.entity.User;
import Event.Event_booking.utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;

    public JwtTokenService(JwtUtil jwtUtil, TokenStore tokenStore) {
        this.jwtUtil = jwtUtil;
        this.tokenStore = tokenStore;
    }

    /**
     * Generates both access and refresh tokens for a user
     * @param user the user entity containing email and role information
     * @return a map containing both accessToken and refreshToken
     */
    public Map<String, String> generateTokens(User user) {
        if (user == null || user.getEmail() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User information is incomplete for token generation");
        }
        
        String email = user.getEmail();
        String role = user.getRole().getName();
        
        String accessToken = jwtUtil.generateToken(email, role);
        String refreshToken = jwtUtil.generateRefreshToken(email, role);
        tokenStore.saveRefreshToken(email, refreshToken, jwtUtil.getRefreshExpirationMs());
        
        return Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken
        );
    }

    /**
     * Generates an access token for a user
     * @param user the user entity
     * @return the access token
     */
    public String generateAccessToken(User user) {
        if (user == null || user.getEmail() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User information is incomplete for token generation");
        }
        return jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
    }

    /**
     * Generates a refresh token for a user
     * @param user the user entity
     * @return the refresh token
     */
    public String generateRefreshToken(User user) {
        if (user == null || user.getEmail() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User information is incomplete for token generation");
        }
        return jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().getName());
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        if (!jwtUtil.isTokenValid(refreshToken)) {
            return false;
        }

        String email = jwtUtil.extractEmail(refreshToken);
        return tokenStore.isRefreshTokenMatch(email, refreshToken);
    }

    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        String email = jwtUtil.extractEmail(refreshToken);
        String role = jwtUtil.extractRole(refreshToken);
        return jwtUtil.generateToken(email, role);
    }

    public boolean logoutRefreshToken(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            return false;
        }

        String email = jwtUtil.extractEmail(refreshToken);
        tokenStore.deleteRefreshToken(email);
        return true;
    }
}
