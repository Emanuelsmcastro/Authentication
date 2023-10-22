package br.com.microservice.statefulanyapi.core.service;

import br.com.microservice.statefulanyapi.core.dto.AnyResponse;
import br.com.microservice.statefulanyapi.core.dto.AuthUserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnyService {

    private final TokenService tokenService;

    public AnyResponse getData(String accessToken) {
        tokenService.validateToken(accessToken);
        AuthUserResponse authUserResponse = tokenService.getAuthUser(accessToken);
        HttpStatus status = HttpStatus.OK;
        return new AnyResponse(status.name(), status.value(), authUserResponse);
    }
}
