package br.com.microservice.statefulanyapi.core.client;

import br.com.microservice.statefulanyapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulanyapi.core.dto.TokenDTO;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(value = "/api/auth")
public interface TokenClient {

    @PostExchange("/token/validate")
    TokenDTO validateToken(@RequestHeader String accessToken);

    @GetExchange("/user")
    public AuthUserResponse getAuthUser(@RequestHeader String accessToken);
}
