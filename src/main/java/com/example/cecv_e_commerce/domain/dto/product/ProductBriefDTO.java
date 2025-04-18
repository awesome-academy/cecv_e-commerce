package com.example.cecv_e_commerce.domain.dto.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductBriefDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName;
}