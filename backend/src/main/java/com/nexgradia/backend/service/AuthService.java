package com.nexgradia.backend.service;

import com.nexgradia.backend.dto.AuthResponse;
import com.nexgradia.backend.dto.LoginRequest;
import com.nexgradia.backend.dto.RegisterRequest;
import com.nexgradia.backend.dto.UserResponse;
import com.nexgradia.backend.exception.EmailAlreadyExistsException;
import com.nexgradia.backend.exception.ForbiddenRoleException;
import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.repository.UserRepository;
import com.nexgradia.backend.security.JwtTokenProvider;
import com.nexgradia.backend.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Enforce constraint 7 & strict allowlist: Public registration may only register non-privileged roles (STUDENT, RECRUITER)
        if (request.getRole() != Role.STUDENT && request.getRole() != Role.RECRUITER) {
            throw new ForbiddenRoleException("Public registration cannot grant privileged role: " + request.getRole());
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(normalizedEmail, hashedPassword, request.getFullName().trim(), request.getRole());

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String token = tokenProvider.generateToken(userPrincipal);

            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole()
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
