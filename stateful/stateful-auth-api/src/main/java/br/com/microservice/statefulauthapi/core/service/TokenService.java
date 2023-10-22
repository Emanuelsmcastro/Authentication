package br.com.microservice.statefulauthapi.core.service;

import br.com.microservice.statefulauthapi.core.dto.TokenData;
import br.com.microservice.statefulauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statefulauthapi.infra.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {

    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;
    private static final Long ONE_DAY_IN_SECONDS = 86400L;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String createToken(String username) {
        String accessToken = UUID.randomUUID().toString();
        TokenData data = new TokenData(username);
        String jsonData = getJsonData(data);
        redisTemplate.opsForValue().set(accessToken, jsonData);
        redisTemplate.expireAt(accessToken, Instant.now().plusSeconds(ONE_DAY_IN_SECONDS));
        return accessToken;
    }

    public TokenData getTokenData(String token) {
        String accessToken = extractToken(token);
        String jsonString = getRedisTokenValue(token);
        try {
            return objectMapper.readValue(jsonString, TokenData.class);
        } catch (Exception e) {
            throw new AuthenticationException("Error extractinf the authenticated user: " + e.getMessage());
        }

    }

    public Boolean validateAccessToken(String token) {
        String accessToken = extractToken(token);
        String jsonString = getRedisTokenValue(accessToken);
        return !ObjectUtils.isEmpty(jsonString);

    }

    public void deleteRedisToken(String token) {
        String accessToken = extractToken(token);
        Boolean deleted = redisTemplate.delete(accessToken);
        if (Boolean.FALSE.equals(deleted))
            throw new ValidationException("Token not found: " + token);
    }

    public String extractToken(String token) {
        if (ObjectUtils.isEmpty(token))
            throw new ValidationException("The access token is invalid.");
        if (token.contains(EMPTY_SPACE))
            return token.split(EMPTY_SPACE)[TOKEN_INDEX];
        return token;
    }

    private String getJsonData(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new ValidationException("Payload Converter error: " + e.getMessage());
        }
    }

    private String getRedisTokenValue(String token) {
        return redisTemplate.opsForValue().get(token);
    }

}
