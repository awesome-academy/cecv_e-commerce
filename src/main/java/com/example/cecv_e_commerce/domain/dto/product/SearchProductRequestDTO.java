package com.example.cecv_e_commerce.domain.dto.product;

import lombok.*;
import java.math.BigDecimal;

@Data
public class SearchProductRequestDTO {
    private String keyword;
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
