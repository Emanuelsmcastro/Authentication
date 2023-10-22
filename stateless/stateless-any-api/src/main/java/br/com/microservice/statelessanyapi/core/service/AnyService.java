package br.com.microservice.statelessanyapi.core.service;

import br.com.microservice.statelessanyapi.core.dto.AnyResponse;
import br.com.microservice.statelessanyapi.core.dto.AuthUserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnyService {

    private final JwtService jwtService;


    public AnyResponse getData(String token) {
        jwtService.validateAccessToken(token);
        AuthUserResponse authUser = jwtService.getAuthenticatedUser(token);
        HttpStatus ok = HttpStatus.OK;
        return new AnyResponse(ok.name(), ok.value(), authUser);
    }
}
