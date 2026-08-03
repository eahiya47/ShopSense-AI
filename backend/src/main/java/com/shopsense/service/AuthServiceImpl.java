package com.shopsense.service;

import com.shopsense.dto.AuthResponse;
import com.shopsense.dto.LoginRequest;
import com.shopsense.dto.RegisterRequest;
import com.shopsense.entity.User;
import com.shopsense.exception.UserAlreadyExistsException;
import com.shopsense.repository.UserRepository;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email address is already in use: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail().toLowerCase().trim(),
                        registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail().toLowerCase().trim(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
