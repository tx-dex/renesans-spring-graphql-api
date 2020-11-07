package fi.sangre.renesans.aaa;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenService {
    private static final String PASSWORD_RESET_CLAIM_KEY = "password_reset";
    private static final String ACCOUNT_ACTIVATION_CLAIM_KEY = "account_activation";

    @Value("${spring.security.jwt.token.secret}")
    private String jwtSecret;
    @Value("${spring.security.jwt.login.token.expirationTimeInMs}")
    private int loginTokenExpirationInMs;
    @Value("#{T(java.time.Duration).ofMillis('${spring.security.jwt.reset.token.expirationTimeInMs}')}")
    private Duration resetTokenDuration;
    @Value("#{T(java.time.Duration).ofMillis('${spring.security.jwt.activation.token.expirationTimeInMs}')}")
    private Duration activationTokenDuration;

    public String generateToken(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + loginTokenExpirationInMs);

        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public String generateResetPasswordToken(final Long userId) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + resetTokenDuration.toMillis());

        final Map<String, Object> claims = new HashMap<>();
        claims.put(PASSWORD_RESET_CLAIM_KEY, true);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateAccountActivationToken(final Long userId) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + activationTokenDuration.toMillis());

        final Map<String, Object> claims = new HashMap<>();
        claims.put(PASSWORD_RESET_CLAIM_KEY, true);
        claims.put(ACCOUNT_ACTIVATION_CLAIM_KEY, true);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateResetPasswordToken(final String token) {
        final Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

        return claims.containsKey(PASSWORD_RESET_CLAIM_KEY) && claims.get(PASSWORD_RESET_CLAIM_KEY, Boolean.class);
    }

    public boolean isActivationToken(final String token) {
        final Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

        return claims.containsKey(ACCOUNT_ACTIVATION_CLAIM_KEY) && claims.get(ACCOUNT_ACTIVATION_CLAIM_KEY, Boolean.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    public Duration getResetTokenExpirationDuration() {
        return resetTokenDuration;
    }

    public Duration getActivationTokenDuration() {
        return activationTokenDuration;
    }
}
