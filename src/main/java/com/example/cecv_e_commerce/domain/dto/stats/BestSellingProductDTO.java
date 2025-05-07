package com.example.cecv_e_commerce.domain.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestSellingProductDTO {
    private Integer productId;
    private String productName;
    private String imageUrl;
    private Long totalQuantitySold;

    public BestSellingProductDTO(Integer productId, String productName, String imageUrl, Number totalQuantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.totalQuantitySold = (totalQuantitySold != null) ? totalQuantitySold.longValue() : 0L;
    }
}
