package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.config.JwtTokenProvider;
import com.example.cecv_e_commerce.domain.dto.user.AuthResponseDTO;
import com.example.cecv_e_commerce.domain.dto.user.LoginRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.RegisterRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.service.CartService;
import com.example.cecv_e_commerce.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CartService cartService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "activationBaseUrl",
                "http://localhost:8080/activate");
        ReflectionTestUtils.setField(authService, "passwordResetBaseUrl",
                "http://localhost:8080/reset-password");
    }

    @Test
    void whenRegisterWithValidData_thenSuccess() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> authService.register(registerRequest));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(mailService).sendActivationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void whenRegisterWithExistingEmail_thenThrowException() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnAuthResponse() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(new UserDTO());

        AuthResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    void whenActivateAccountWithValidToken_thenSuccess() {
        String token = "valid-token";
        User user = new User();
        user.setEmail("test@example.com");
        user.setActivationToken(token);
        user.setActivationDeadline(LocalDateTime.now().plusHours(1));

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.activateAccount(token));

        verify(userRepository).findByActivationToken(token);
        verify(userRepository).save(any(User.class));
        verify(cartService).createCart(any(User.class));
    }

    @Test
    void whenActivateAccountWithExpiredToken_thenThrowException() {
        String token = "expired-token";
        User user = new User();
        user.setEmail("test@example.com");
        user.setActivationToken(token);
        user.setActivationDeadline(LocalDateTime.now().minusHours(1));

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.activateAccount(token));
        verify(userRepository).findByActivationToken(token);
        verify(userRepository).delete(user);
    }

    @Test
    void whenRequestPasswordResetWithValidEmail_thenSuccess() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.requestPasswordReset(email));

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
        verify(mailService).sendPasswordResetEmail(eq(email), eq("Test User"), anyString());
    }

    @Test
    void whenRequestPasswordResetWithInactiveAccount_thenThrowException() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.requestPasswordReset(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenResetPasswordWithValidToken_thenSuccess() {
        String token = "valid-token";
        String newPassword = "newPassword123";
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordResetToken(token);
        user.setPasswordResetDeadline(LocalDateTime.now().plusHours(1));

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.resetPassword(token, newPassword));

        verify(userRepository).findByPasswordResetToken(token);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenResetPasswordWithExpiredToken_thenThrowException() {
        String token = "expired-token";
        String newPassword = "newPassword123";
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordResetToken(token);
        user.setPasswordResetDeadline(LocalDateTime.now().minusHours(1));

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword(token, newPassword));
        verify(userRepository).findByPasswordResetToken(token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenLogout_thenSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    void whenRegisterAndSendMailFails_thenStillRegistersUser() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("failmail@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Mail error")).when(mailService)
                .sendActivationEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> authService.register(registerRequest));
        verify(userRepository).save(any(User.class));
        verify(mailService).sendActivationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void whenRequestPasswordResetAndSendMailFails_thenStillUpdatesUser() {
        String email = "failmail@example.com";
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("Mail error")).when(mailService)
                .sendPasswordResetEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> authService.requestPasswordReset(email));
        verify(userRepository).save(any(User.class));
        verify(mailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void whenActivateAccountAlreadyActive_thenThrowException() {
        String token = "active-token";
        User user = new User();
        user.setActivationToken(token);
        user.setActivationDeadline(LocalDateTime.now().plusHours(1));
        user.setActive(true);

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.activateAccount(token));
    }

    @Test
    void whenResetPasswordWithInvalidToken_thenThrowException() {
        String token = "invalid-token";
        String newPassword = "newPassword123";
        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.resetPassword(token, newPassword));
    }

    @Test
    void whenLogoutWithoutAuthentication_thenNoException() {
        SecurityContextHolder.clearContext();
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    void whenLogoutWithAnonymousAuthentication_thenNoException() {
        Authentication anonymous = mock(
                org.springframework.security.authentication.AnonymousAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(anonymous);
        assertDoesNotThrow(() -> authService.logout());
    }
}
