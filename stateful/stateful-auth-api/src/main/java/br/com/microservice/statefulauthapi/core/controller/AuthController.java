package br.com.microservice.statefulauthapi.core.controller;

import br.com.microservice.statefulauthapi.core.dto.AuthRequest;
import br.com.microservice.statefulauthapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulauthapi.core.dto.TokenDTO;
import br.com.microservice.statefulauthapi.core.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/login")
    public TokenDTO login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/token/validate")
    public TokenDTO validateToken(@RequestHeader String accessToken) {
        return authService.validateToken(accessToken);
    }

    @GetMapping("/logout")
    public Map<String, Object> logout(@RequestHeader String accessToken) {
        authService.Logout(accessToken);
        Map<String, Object> response = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        response.put("Status", status.name());
        response.put("code", status.value());
        return response;
    }

    @GetMapping("/user")
    public AuthUserResponse getAuthUser(@RequestHeader String accessToken) {
        return authService.getAuthUser(accessToken);
    }

}
