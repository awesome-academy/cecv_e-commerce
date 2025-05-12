package com.example.cecv_e_commerce.domain.dto.category;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}

