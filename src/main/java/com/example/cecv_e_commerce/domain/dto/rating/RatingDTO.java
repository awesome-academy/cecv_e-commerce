package com.example.cecv_e_commerce.domain.dto.rating;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Integer id;
    private Integer rating;
    private LocalDateTime createdAt;
    private Integer productId;
    private Integer userId;
    private String username;
}
