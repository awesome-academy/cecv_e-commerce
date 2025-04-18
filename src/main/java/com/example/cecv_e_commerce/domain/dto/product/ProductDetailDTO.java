package com.example.cecv_e_commerce.domain.dto.product;

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
    private String categoryName;
    private Integer quantity;
    private Integer categoryId;
    private boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}