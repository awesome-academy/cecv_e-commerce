package com.example.cecv_e_commerce.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDTO user;

    public AuthResponseDTO(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
