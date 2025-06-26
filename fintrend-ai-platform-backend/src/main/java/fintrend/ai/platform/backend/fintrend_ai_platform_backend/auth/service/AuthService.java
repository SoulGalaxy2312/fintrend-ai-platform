package fintrend.ai.platform.backend.fintrend_ai_platform_backend.auth.service;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.user.model.User;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.user.repository.UserRepository;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.security.Role;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.security.JwtTokenProvider;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.auth.request.AuthRequestDTO;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.exception.UsernameAlreadyExistsException;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.response.StatusResponseDTO;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public StatusResponseDTO register(AuthRequestDTO dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);
        return new StatusResponseDTO(true, "User registered successfully");
    }

    public String login(AuthRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return jwtTokenProvider.generateToken(userDetails);
    }
}
