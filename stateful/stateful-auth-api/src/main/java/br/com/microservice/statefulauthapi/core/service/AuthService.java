package br.com.microservice.statefulauthapi.core.service;

import br.com.microservice.statefulauthapi.core.dto.AuthRequest;
import br.com.microservice.statefulauthapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulauthapi.core.dto.TokenDTO;
import br.com.microservice.statefulauthapi.core.dto.TokenData;
import br.com.microservice.statefulauthapi.core.model.User;
import br.com.microservice.statefulauthapi.core.repository.UserRepository;
import br.com.microservice.statefulauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statefulauthapi.infra.exception.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;


    public TokenDTO login(AuthRequest request) {
        User user = findByUserName(request.username());
        String accessToken = tokenService.createToken(user.getUsername());
        validatePassword(request.password(), user.getPassword());
        return new TokenDTO(accessToken);
    }

    public TokenDTO validateToken(String token) {
        String accessToken = tokenService.extractToken(token);
        validateExistingToken(accessToken);
        Boolean isValid = tokenService.validateAccessToken(token);
        if (Boolean.FALSE.equals(isValid))
            throw new AuthenticationException("Invalid token");
        return new TokenDTO(token);
    }

    public AuthUserResponse getAuthUser(String token) {
        String accessToken = tokenService.extractToken(token);
        TokenData tokenData = tokenService.getTokenData(accessToken);
        User user = userRepository.findByUsername(tokenData.username())
                .orElseThrow(() -> new AuthenticationException("User not found"));
        return new AuthUserResponse(user.getId(), user.getUsername());
    }

    public void Logout(String token) {
        String accessToken = tokenService.extractToken(token);
        tokenService.deleteRedisToken(accessToken);
    }

    private User findByUserName(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

    }

    private void validateExistingToken(String accessToken) {
        if (ObjectUtils.isEmpty(accessToken))
            throw new ValidationException("Token is empty");
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (ObjectUtils.isEmpty(rawPassword))
            throw new ValidationException("The password must be informed");
        if (!passwordEncoder.matches(rawPassword, encodedPassword))
            throw new ValidationException("The password is incorrect");
    }
}
