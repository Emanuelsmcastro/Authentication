package br.com.microservice.statelessauthapi.core.model.service;

import br.com.microservice.statelessauthapi.core.model.User;
import br.com.microservice.statelessauthapi.core.model.dto.AuthRequest;
import br.com.microservice.statelessauthapi.core.model.dto.TokenDTO;
import br.com.microservice.statelessauthapi.core.repository.UserRepository;
import br.com.microservice.statelessauthapi.infra.exception.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TokenDTO login(AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ValidationException("User not found."));
        String accessToken = jwtService.createToken(user);
        validatePassword(request.password(), user.getPassword());
        return new TokenDTO(accessToken);
    }

    public TokenDTO validateToken(String accessToken) {
        validateExistingToken(accessToken);
        jwtService.validateAccessToken(accessToken);
        return new TokenDTO(accessToken);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (ObjectUtils.isEmpty(rawPassword))
            throw new ValidationException("Empty password.");
        if (!passwordEncoder.matches(rawPassword, encodedPassword))
            throw new ValidationException("Incorrect password.");
    }

    private void validateExistingToken(String accessToken) {
        if (ObjectUtils.isEmpty(accessToken))
            throw new ValidationException("Token not found.");
    }

}
