package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.user.*;
import com.example.cecv_e_commerce.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDto) {
        authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully! Please check your email for activation link."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequestDto) {
        AuthResponseDTO authResponse = authService.login(loginRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestParam("token") String token) {
        authService.activateAccount(token);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody RequestPasswordResetDTO requestDto) {
        authService.requestPasswordReset(requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully! Please check your inbox.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody PasswordResetRequestDTO resetDto) {
        authService.resetPassword(resetDto.getToken(), resetDto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success( "Password has been reset successfully!"));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> logoutUser() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful."));
    }
}
