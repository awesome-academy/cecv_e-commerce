package com.example.cecv_e_commerce.authentication;

import com.example.cecv_e_commerce.domain.dto.user.LoginRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.RegisterRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.AuthResponseDTO;
import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.service.AuthService;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenLoginWithValidCredentials_thenReturnAuthResponse() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setName("Test User");

        AuthResponseDTO authResponse = new AuthResponseDTO("jwt-token", userDTO);

        Mockito.when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.name").value("Test User"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnUnauthorized() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("wrong@example.com");
        loginRequest.setPassword("invalid");

        Mockito.when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void whenRegisterWithValidData_thenReturnAuthResponse() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("New User");

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("newuser@example.com");
        userDTO.setName("New User");

        AuthResponseDTO authResponse = new AuthResponseDTO("jwt-token", userDTO);

        doNothing().when(authService).register(any(RegisterRequestDTO.class));
        Mockito.when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "User registered successfully! Please check your email for activation link."));
    }

    @Test
    void whenRegisterWithInvalidData_thenReturnBadRequest() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturnBadRequest() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Existing User");

        doThrow(new BadRequestException("Email already exists")).when(authService)
                .register(any(RegisterRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void whenForgotPassword_thenReturnSuccess() throws Exception {
        var request = new com.example.cecv_e_commerce.domain.dto.user.RequestPasswordResetDTO();
        request.setEmail("test@example.com");
        doNothing().when(authService).requestPasswordReset("test@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.message")
                        .value("Password reset email sent successfully! Please check your inbox."));
    }

    @Test
    void whenResetPassword_thenReturnSuccess() throws Exception {
        var request = new com.example.cecv_e_commerce.domain.dto.user.PasswordResetRequestDTO();
        request.setToken("token123");
        request.setNewPassword("newPassword123");
        doNothing().when(authService).resetPassword("token123", "newPassword123");

        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password has been reset successfully!"));
    }

    @Test
    void whenActivateAccount_thenReturnSuccess() throws Exception {
        doNothing().when(authService).activateAccount("token123");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/v1/auth/activate?token=token123")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account activated successfully!"));
    }

    @Test
    @WithMockUser
    void whenLogout_thenReturnSuccess() throws Exception {
        doNothing().when(authService).logout();
        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful."));
    }
}
