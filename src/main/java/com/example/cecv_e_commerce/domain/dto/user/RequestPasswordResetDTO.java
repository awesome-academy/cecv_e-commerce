package com.example.cecv_e_commerce.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestPasswordResetDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
