package br.com.microservice.statefulanyapi.core.service;

import br.com.microservice.statefulanyapi.core.client.TokenClient;
import br.com.microservice.statefulanyapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulanyapi.core.dto.TokenDTO;
import br.com.microservice.statefulanyapi.infra.exception.AuthenticationException;
import br.com.microservice.statefulanyapi.infra.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TokenService {

    private final TokenClient tokenClient;

    public void validateToken(String token) {
        try {
            log.info("Sending request for token validation {}", token);
            TokenDTO tokenDTO = tokenClient.validateToken(token);
            log.info("Token {} is valid", tokenDTO.accessToken());
        } catch (Exception e) {
            throw new ValidationException("Validation token error: " + e.getMessage());
        }
    }

    public AuthUserResponse getAuthUser(String token) {
        try {
            log.info("Sending request for {}", token);
            AuthUserResponse response = tokenClient.getAuthUser(token);
            log.info("Auth user found: {}", response.toString());
            return response;
        } catch (Exception e) {
            throw new AuthenticationException("Validation token error: " + e.getMessage());
        }
    }
}
