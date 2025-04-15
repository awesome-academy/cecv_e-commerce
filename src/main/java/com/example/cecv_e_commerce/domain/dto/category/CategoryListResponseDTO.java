package com.example.cecv_e_commerce.domain.dto.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResponseDTO {
    private List<CategoryDTO> categories;
    private int categoriesCount;
}
