package com.example.cecv_e_commerce.domain.dto.product;

import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductBriefDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private CategoryDTO category;
}
