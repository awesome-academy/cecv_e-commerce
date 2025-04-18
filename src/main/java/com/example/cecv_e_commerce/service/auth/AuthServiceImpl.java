package com.example.cecv_e_commerce.service.auth;

import com.example.cecv_e_commerce.config.JwtTokenProvider;
import com.example.cecv_e_commerce.domain.dto.user.AuthResponseDTO;
import com.example.cecv_e_commerce.domain.dto.user.LoginRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.RegisterRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.model.Role;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.exception.BadRequestException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private static final int PASSWORD_RESET_TOKEN_EXPIRATION_HOURS = 1;
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${app.activation.base-url}")
    private String activationBaseUrl;

    @Value("${app.password-reset.base-url}")
    private String passwordResetBaseUrl;


    @Override
    @Transactional
    public void register(RegisterRequestDTO registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new BadRequestException("Error: Email is already taken!");
        }

        User user = new User();
        user.setName(registerRequestDto.getName());
        user.setEmail(registerRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.setRole(Role.USER);
        user.setActive(false);

        // Generate activation token
        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        user.setActivationDeadline(LocalDateTime.now().plusHours(24)); // Token hết hạn sau 24h

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", savedUser.getEmail());
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        UserDTO userDto = modelMapper.map(userDetails, UserDTO.class);

        logger.info("User logged in successfully: {}", userDetails.getEmail());
        return new AuthResponseDTO(jwt, userDto);
    }

    @Override
    @Transactional
    public void activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid activation token."));

        if (user.getActivationDeadline().isBefore(LocalDateTime.now())) {
            userRepository.delete(user);
            logger.warn("Activation token expired for token: {}", token);
            throw new BadRequestException("Activation token has expired. Please register again.");
        }

        if (user.isActive()) {
            throw new BadRequestException("Account is already activated.");
        }

        user.setActive(true);
        user.setActivatedAt(LocalDateTime.now());
        user.setActivationToken(null); // Delete token after active
        user.setActivationDeadline(null);
        userRepository.save(user);
        logger.info("Account activated successfully for email: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new BadRequestException("Account is not active. Please activate your account first.");
        }

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetDeadline(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_EXPIRATION_HOURS));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token."));

        if (user.getPasswordResetDeadline().isBefore(LocalDateTime.now())) {
            user.setPasswordResetToken(null); // Delete expired token
            user.setPasswordResetDeadline(null);
            userRepository.save(user);
            logger.warn("Password reset token expired for token: {}", token);
            throw new BadRequestException("Password reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null); // Delete token after reset
        user.setPasswordResetDeadline(null);
        userRepository.save(user);
        logger.info("Password reset successfully for email: {}", user.getEmail());
        // Todo: send mail: mailService.sendPasswordChangedEmail(user.getEmail(), user.getName());
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            logger.info("Logout successful for user: {}. Client should clear the token.", currentUserName);
        } else {
            logger.warn("Logout endpoint called, but no authenticated user found in Security Context.");
        }
    }
}
