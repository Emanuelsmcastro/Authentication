package br.com.microservice.statelessauthapi.core.model.service;

import br.com.microservice.statelessauthapi.core.model.User;
import br.com.microservice.statelessauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statelessauthapi.infra.exception.ValidationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Integer EXPIRATION_TOKEN_IN_HOURS = 24;
    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;

    @Value("${app.token.secret-key}")
    private String secretKey;

    public String createToken(User user) {
        Map<String, String> data = new HashMap<>();
        data.put("id", user.getId().toString());
        data.put("username", user.getUsername());
        return Jwts.builder()
                .setClaims(data)
                .setExpiration(generateExpiresAt())
                .signWith(generateSign())
                .compact();
    }

    public void validateAccessToken(String token) {
        String accessToken = extractToken(token);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(generateSign())
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (Exception e) {
            throw new AuthenticationException("Invalid token: " + e.getMessage());
        }
    }

    private Date generateExpiresAt() {
        return Date.from(
                LocalDateTime.now()
                        .plusHours(EXPIRATION_TOKEN_IN_HOURS)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    private SecretKey generateSign() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private String extractToken(String token) {
        if (ObjectUtils.isEmpty(token))
            throw new ValidationException("The access token was not informed.");
        if (token.contains(EMPTY_SPACE))
            return token.split(EMPTY_SPACE)[TOKEN_INDEX];
        return token;
    }
}
