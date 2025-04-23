package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;

public interface RatingService {
    RatingDTO addRating(User user, Product product, Integer rating);
}
