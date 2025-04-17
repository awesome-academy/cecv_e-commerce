package com.example.cecv_e_commerce.service.auth;

import com.example.cecv_e_commerce.domain.dto.user.AuthResponseDTO;
import com.example.cecv_e_commerce.domain.dto.user.LoginRequestDTO;
import com.example.cecv_e_commerce.domain.dto.user.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO registerRequestDto);

    AuthResponseDTO login(LoginRequestDTO loginRequestDto);

    void activateAccount(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void logout();
}
