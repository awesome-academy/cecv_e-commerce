package com.example.cecv_e_commerce.domain.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserStatusUpdateDTO {

    @NotNull(message = "isActive status cannot be null")
    private Boolean isActive;
}
