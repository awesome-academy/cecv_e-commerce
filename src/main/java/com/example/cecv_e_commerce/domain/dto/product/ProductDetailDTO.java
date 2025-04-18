package com.example.cecv_e_commerce.domain.dto.product;

import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDetailDTO {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private CategoryDTO category;
    private Integer quantity;
    private boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
