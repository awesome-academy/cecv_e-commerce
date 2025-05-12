package com.example.cecv_e_commerce.domain.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestUpdateDTO {
    @NotBlank
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
}
