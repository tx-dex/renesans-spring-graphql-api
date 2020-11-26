package fi.sangre.renesans.aaa;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.sangre.renesans.application.utils.TokenUtils.PASSWORD_RESET_CLAIM_KEY;

@Slf4j
@Component
public class JwtTokenService {
    @Value("${spring.security.jwt.token.secret}")
    private String jwtSecret;
    @Value("${spring.security.jwt.login.token.expirationTimeInMs}")
    private int loginTokenExpirationInMs;

    @NonNull
    public String generateToken(@NonNull final String subject,
                                @NonNull final Map<String, Object> claims,
                                @NonNull final Long expirationTime) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(new HashMap<>(claims))
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


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

    public boolean validateResetPasswordToken(final String token) {
        final Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

        return claims.containsKey(PASSWORD_RESET_CLAIM_KEY) && claims.get(PASSWORD_RESET_CLAIM_KEY, Boolean.class);
    }

    @Nullable
    public Jws<Claims> getClaims(@NonNull final String token) {
        if (StringUtils.hasText(token)) {
            try {
                return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
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
        }

        return null;
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
}
