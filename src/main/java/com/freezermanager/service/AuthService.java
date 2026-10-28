
package com.freezermanager.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.user.User;
import com.freezermanager.dto.AuthResponse;
import com.freezermanager.dto.RegisterRequest;
import com.freezermanager.exception.AlreadyExistException;
import com.freezermanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new AlreadyExistException(
                    "Ya existe un usuario registrado con ese email."
            );
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                jwtService.generateToken(savedUser.getEmail()),
                "Bearer"
        );
    }

    public AuthResponse login(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                )
        );

        return new AuthResponse(
                jwtService.generateToken(authentication.getName()),
                "Bearer"
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
