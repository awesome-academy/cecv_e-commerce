package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.review.ReviewRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewResponseDTO;
import com.example.cecv_e_commerce.domain.model.User;

public interface ReviewCoordinatorService {
    ReviewResponseDTO addReviewAndRating(Integer productId, ReviewRequestDTO request, User currentUser);
}
