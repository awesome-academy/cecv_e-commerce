package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;

public interface RatingService {
    RatingDTO addRating(Integer userId, Integer productId, Integer rating);
}
