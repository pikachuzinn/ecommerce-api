package dev.henan.ecommerce.auth;

import dev.henan.ecommerce.auth.dto.LoginRequest;
import dev.henan.ecommerce.auth.dto.RegisterRequest;
import dev.henan.ecommerce.auth.dto.TokenResponse;
import dev.henan.ecommerce.auth.dto.UserResponse;
import dev.henan.ecommerce.common.exception.BusinessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Ja existe uma conta cadastrada com este e-mail.");
        }

        User user = new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                Set.of(Role.ROLE_CUSTOMER));

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Autentica e devolve o access token. Delegar ao AuthenticationManager mantem
     * a comparacao de senha em tempo constante e centraliza o tratamento de falha.
     */
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()));

        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        return TokenResponse.bearer(tokenService.generateToken(principal), tokenService.getExpirationSeconds());
    }
}
