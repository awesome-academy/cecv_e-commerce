package com.example.cecv_e_commerce.domain.dto.category;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Integer id;

    private String name;

    private String description;

    private LocalDateTime createdAt;
}
