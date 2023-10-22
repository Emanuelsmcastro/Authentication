package br.com.microservice.statelessauthapi.core.controller;

import br.com.microservice.statelessauthapi.core.model.dto.AuthRequest;
import br.com.microservice.statelessauthapi.core.model.dto.TokenDTO;
import br.com.microservice.statelessauthapi.core.model.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/login")
    public TokenDTO login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping(value = "/token/validate")
    public TokenDTO login(@RequestHeader String accessToken) {
        return authService.validateToken(accessToken);
    }
}
