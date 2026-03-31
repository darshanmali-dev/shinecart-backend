package com.college.shinecart.service;

import com.college.shinecart.dto.AuthResponse;
import com.college.shinecart.dto.LoginRequest;
import com.college.shinecart.dto.RegisterRequest;
import com.college.shinecart.entity.User;
import com.college.shinecart.exception.BadRequestException;
import com.college.shinecart.exception.ResourceAlreadyExistsException;
import com.college.shinecart.exception.UnauthorizedException;
import com.college.shinecart.exception.UserAlreadyExistsException;
import com.college.shinecart.repository.UserRepository;
import com.college.shinecart.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        // Check if user already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(req.getUsername())){
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        if(req.getPhone().length() != 10){
            throw new BadRequestException("Phone number must be exactly 10 digits");
        }

        // Create new user
        User user = User.builder()
                .username(req.getUsername())  // Full name
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(List.of("ROLE_USER"))  // Default role
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(user);

        // Build response
        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest req) {
        // Find user by email
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if(!user.isEnabled()){
            throw new BadRequestException("User is Disabled by Admin");
        }

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),  // Spring Security uses username
                            req.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new BadRequestException("Invalid email or password");
        }

        // Generate token
        String token = jwtUtil.generateToken(user);

        // Build response
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        // Get role without "ROLE_" prefix
        String role = user.getRoles().get(0)
                .replace("ROLE_", "")
                .toLowerCase();

        // Build UserDTO
        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getUsername())  // Full name
                .email(user.getEmail())
                .role(role)
                .avatar(null)  // Add avatar field to User entity if needed
                .build();

        // Build AuthResponse
        return AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }
}