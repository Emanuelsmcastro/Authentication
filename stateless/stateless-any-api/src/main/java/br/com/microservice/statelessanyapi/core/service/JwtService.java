package br.com.microservice.statelessanyapi.core.service;

import br.com.microservice.statelessanyapi.core.dto.AuthUserResponse;
import br.com.microservice.statelessanyapi.infra.exception.AuthenticationException;
import br.com.microservice.statelessanyapi.infra.exception.ValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;

    @Value("${app.token.secret-key}")
    private String secretKey;

    public AuthUserResponse getAuthenticatedUser(String token) {
        Claims tokenClaims = getClaims(token);
        Integer userId = Integer.valueOf((String) tokenClaims.get("id"));
        return new AuthUserResponse(userId, (String) tokenClaims.get("username"));
    }

    public void validateAccessToken(String token) {
        getClaims(token);
    }

    private Claims getClaims(String token) {
        String accessToken = extractToken(token);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(generateSign())
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (Exception e) {
            throw new AuthenticationException("Invalid token: " + e.getMessage());
        }
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
